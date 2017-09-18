package uk.gov.justice.services.eventsourcing.source.api;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.FIRST;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.HEAD;
import static uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider.SYSTEM_USER_ID;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.rest.ForbiddenRequestExceptionMapper;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventPageResource;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventSourceApiApplication;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsPageService;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsService;
import uk.gov.justice.services.eventsourcing.source.api.util.LoggerProducer;
import uk.gov.justice.services.eventsourcing.source.api.util.OpenEjbAwareEventRepository;
import uk.gov.justice.services.eventsourcing.source.api.util.OpenEjbAwareEventStreamRepository;
import uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.sql.DataSource;

import com.jayway.jsonpath.JsonPath;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class EventsPageIT {
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/event-source-api/rest";
    private static final UUID STREAM_ID = randomUUID();
    private static final String EVENT_STREAM_URL_PATH_PREFIX = "/event-source-api/rest/event-streams/" + STREAM_ID;
    public static final int PAGE_SIZE = 2;
    private static int port = -1;


    private CloseableHttpClient httpClient;
    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private OpenEjbAwareEventRepository eventsRepository;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Before
    public void setup() throws Exception {
        httpClient = HttpClients.createDefault();
        initEventDatabase();
    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .build();
    }

    @ApplicationScoped
    public static class TestEventInsertionStrategyProducer {

        @Produces
        public EventInsertionStrategy eventLogInsertionStrategy() {
            return new AnsiSQLEventLogInsertionStrategy();
        }
    }

    @Module
    @Classes(cdi = true, value = {
            ObjectMapperProducer.class,
            ObjectToJsonValueConverter.class,
            EventPageResource.class,
            OpenEjbAwareEventRepository.class,
            EventsService.class,
            AccessController.class,
            TestSystemUserProvider.class,
            ForbiddenRequestExceptionMapper.class,
            TestEventInsertionStrategyProducer.class,
            OpenEjbAwareEventStreamRepository.class,
            EventsPageService.class,
            LoggerProducer.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("event-source-api")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", EventSourceApiApplication.class.getName());
    }

    @Test
    public void shouldReturnTheFullEventInfo() throws Exception {
        final UUID streamId = randomUUID();

        final ZonedDateTime event3CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), event3CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, "3", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name3"))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[0].createdAt", is(event3CreatedAt.toString()))
                .assertThat("$.data[0].payload.field3", is("value3"))

                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].name", containsString("Test Name2"))
                .assertThat("$.data[1].sequenceId", is(2))
                .assertThat("$.data[1].createdAt", is(event2CreatedAt.toString()))
                .assertThat("$.data[1].payload.field2", is("value2"));
    }

    @Test
    public void shouldReturnHeadNextEvents() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, HEAD.getPosition(), BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(5))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(4));

        with(value)
                .assertNotDefined("$.pagingLinks.previous");

        final String nextPageUrl = JsonPath.read(value, "$.pagingLinks.next");

        assertThat(nextPageUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(value);
    }

    @Test
    public void shouldThrowExceptionWhenHeadRequestedWithPreviousAsDirection() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, HEAD.getPosition(), FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnLatestEvents() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "5", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(5))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(4));

        with(value)
                .assertNotDefined("$.pagingLinks.previous");

        final String nextPageUrl = JsonPath.read(value, "$.pagingLinks.next");

        assertThat(nextPageUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(value);
    }


    @Test
    public void shouldReturnLastOrOldestEvents() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, FIRST.getPosition(), FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(2))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(1));

        final String previousPageUrl = JsonPath.read(value, "$.pagingLinks.previous");

        assertThat(previousPageUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/FORWARD/" + PAGE_SIZE));

        with(value)
                .assertNotDefined("$.pagingLinks.next");

        assertHeadAndLastLinks(value);
    }

    @Test
    public void shouldThrowExceptionWhenLastRequestedWithNextAsDirection() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, FIRST.getPosition(), BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnNextEventsWithPreviousAndNextLinks() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(3))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(2));

        final String previousPageUrl = JsonPath.read(value, "$.pagingLinks.previous");

        assertThat(previousPageUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/4/FORWARD/" + PAGE_SIZE));

        final String nextPageUrl = JsonPath.read(value, "$.pagingLinks.next");

        assertThat(nextPageUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(value);
    }


    @Test
    public void shouldReturnPreviousEventsWithPreviousAndNextLinks() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(4))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(3));

        final String previousPageUrl = JsonPath.read(value, "$.pagingLinks.previous");

        assertThat(previousPageUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/5/FORWARD/" + PAGE_SIZE));

        final String nextPageUrl = JsonPath.read(value, "$.pagingLinks.next");

        assertThat(nextPageUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/2/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(value);
    }


    @Test
    public void shouldNotReturnRecordsForUnknownSequenceId() throws Exception {

        storeEvents();

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "6", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(0));

        assertHeadAndLastLinks(value);
    }

    @Test
    public void shouldReturnEmptyFeedIfNoDataAndNoPreviousAndNextLinks() throws IOException {

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "5", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(0));

        with(value)
                .assertNotDefined("$.pagingLinks.previous");

        with(value)
                .assertNotDefined("$.pagingLinks.next");


        assertHeadAndLastLinks(value);
    }

    @Test
    public void shouldGoNextFromPage2FromHead() throws Exception {

        storeEvents();

        final HttpResponse firstPageResponse = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, HEAD.getPosition(), BACKWARD, PAGE_SIZE);

        assertThat(firstPageResponse.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String firstPage = responseBodyOf(firstPageResponse);

        with(firstPage)
                .assertNotDefined("$.pagingLinks.previous");

        final String nextPageUrlOfFirstPage = JsonPath.read(firstPage, "$.pagingLinks.next");

        assertThat(nextPageUrlOfFirstPage, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        final HttpResponse nextPageResponse = feedOf(nextPageUrlOfFirstPage, SYSTEM_USER_ID);

        final String nextPage = responseBodyOf(nextPageResponse);

        with(nextPage)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(2));

        final String previousPageUrlOfNextPage = JsonPath.read(nextPage, "$.pagingLinks.previous");
        assertThat(previousPageUrlOfNextPage, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/4/FORWARD/" + PAGE_SIZE));

        final String nextPageUrlOfNextPage = JsonPath.read(nextPage, "$.pagingLinks.next");
        assertThat(nextPageUrlOfNextPage, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(nextPage);
    }


    @Test
    public void shouldGoNextToPage3FromPage2() throws Exception {

        storeEvents();

        final HttpResponse secondPageResponse = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", BACKWARD, PAGE_SIZE);

        final String page2 = responseBodyOf(secondPageResponse);

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(2));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.next");

        final HttpResponse nextPageResponse = feedOf(page3Url, SYSTEM_USER_ID);

        final String page3 = responseBodyOf(nextPageResponse);

        with(page3)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(2))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(1));

        with(page3)
                .assertNotDefined("$.pagingLinks.next");
        final String previousPageUrlOfNextPage = JsonPath.read(page3, "$.pagingLinks.previous");
        assertThat(previousPageUrlOfNextPage, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/FORWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page3);
    }

    @Test
    public void shouldGoPreviousPage1FromPage2() throws Exception {

        storeEvents();

        final HttpResponse secondPageResponse = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", BACKWARD, PAGE_SIZE);

        final String page2 = responseBodyOf(secondPageResponse);

        with(page2)
                .assertThat("$.data", hasSize(PAGE_SIZE))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(PAGE_SIZE));

        final String page2Url = JsonPath.read(page2, "$.pagingLinks.previous");

        final HttpResponse previousPageResponse = feedOf(page2Url, SYSTEM_USER_ID);

        final String page1 = responseBodyOf(previousPageResponse);

        with(page1)
                .assertThat("$.data", hasSize(PAGE_SIZE))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].sequenceId", is(5))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].sequenceId", is(4));

        with(page1)
                .assertNotDefined("$.pagingLinks.previous");

        final String page2NextUrl = JsonPath.read(page1, "$.pagingLinks.next");
        assertThat(page2NextUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page1);
    }

    @Test
    public void shouldReturnForbiddenIfNotASystemUser() throws IOException {
        final HttpResponse response = eventsFeedFor(randomUUID(), randomUUID(), "12", BACKWARD, PAGE_SIZE);
        assertThat(response.getStatusLine().getStatusCode(), is(FORBIDDEN.getStatusCode()));
    }

    private String responseBodyOf(final HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private HttpResponse eventsFeedFor(final UUID userId,
                                       final UUID streamId,
                                       final String position,
                                       final Direction link,
                                       final long pageSize) throws IOException {
        final String url = format(BASE_URI_PATTERN + "/event-streams/" + streamId.toString() + "/" + position + "/" + link
                + "/" + pageSize, port);
        return feedOf(url, userId);
    }

    private HttpResponse feedOf(final String url, final UUID userId) throws IOException {
        final HttpUriRequest request = new HttpGet(url);
        request.addHeader(USER_ID, userId.toString());
        return httpClient.execute(request);
    }

    private void initEventDatabase() throws Exception {

        Liquibase eventStoreLiquibase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        eventStoreLiquibase.dropAll();
        eventStoreLiquibase.update("");
    }

    private void storeEvents() throws InvalidSequenceIdException {
        final Event event5 = new Event(randomUUID(), STREAM_ID, 5L, "Test Name5", METADATA_JSON, createObjectBuilder().add("field5", "value5").build().toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), STREAM_ID, 4L, "Test Name4", METADATA_JSON, createObjectBuilder().add("field4", "value4").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), STREAM_ID, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), STREAM_ID, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), STREAM_ID, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);
    }

    private void assertHeadAndLastLinks(String value) {
        final String headUrl = JsonPath.read(value, "$.pagingLinks.head");
        assertThat(headUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/HEAD/BACKWARD/" + PAGE_SIZE));

        final String firstUrl = JsonPath.read(value, "$.pagingLinks.first");
        assertThat(firstUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));
    }
}
