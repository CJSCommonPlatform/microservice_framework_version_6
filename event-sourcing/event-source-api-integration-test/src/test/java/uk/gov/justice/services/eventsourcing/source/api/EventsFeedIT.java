package uk.gov.justice.services.eventsourcing.source.api;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider.SYSTEM_USER_ID;

import uk.gov.justice.services.common.rest.ForbiddenRequestExceptionMapper;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventFeedResource;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventSourceApiApplication;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventStreamsFeedResource;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.util.LoggerProducer;
import uk.gov.justice.services.eventsourcing.source.api.util.OpenEjbAwareEventRepository;
import uk.gov.justice.services.eventsourcing.source.api.util.OpenEjbAwareEventStreamRepository;
import uk.gov.justice.services.eventsourcing.source.api.util.TestEventStreamsFeedService;
import uk.gov.justice.services.eventsourcing.source.api.util.TestEventsFeedService;
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
public class EventsFeedIT {
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/event-source-api/rest";
    private static int port = -1;

    private CloseableHttpClient httpClient;
    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private OpenEjbAwareEventRepository eventsRepository;

    @Inject
    private TestEventsFeedService eventFeedService;

    @Inject
    private OpenEjbAwareEventStreamRepository eventStreamRepository;

    @Inject
    private TestEventStreamsFeedService eventStreamsFeedService;

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

    @Module
    @Classes(cdi = true, value = {
            EventFeedResource.class,
            OpenEjbAwareEventRepository.class,
            TestEventsFeedService.class,
            AccessController.class,
            TestSystemUserProvider.class,
            ForbiddenRequestExceptionMapper.class,
            TestEventInsertionStrategyProducer.class,
            EventStreamsFeedResource.class,
            OpenEjbAwareEventStreamRepository.class,
            TestEventStreamsFeedService.class,
            LoggerProducer.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("event-source-api")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", EventSourceApiApplication.class.getName());
    }

    @Before
    public void setUp() throws Exception {
        eventFeedService
                .initialiseWithPageSize(25);
    }

    @Test
    public void shouldReturnFirstPageOfFeed() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize(3);

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();
        final ZonedDateTime event3CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), event3CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(3))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name1"))
                .assertThat("$.data[0].sequenceId", is(1))
                .assertThat("$.data[0].createdAt", is(event1CreatedAt.toString()))
                .assertThat("$.data[0].payload.field1", is("value1"))

                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].name", containsString("Test Name2"))
                .assertThat("$.data[1].sequenceId", is(2))
                .assertThat("$.data[1].createdAt", is(event2CreatedAt.toString()))
                .assertThat("$.data[1].payload.field2", is("value2"))

                .assertThat("$.data[2].streamId", is(streamId.toString()))
                .assertThat("$.data[2].name", containsString("Test Name3"))
                .assertThat("$.data[2].sequenceId", is(3))
                .assertThat("$.data[2].createdAt", is(event3CreatedAt.toString()))
                .assertThat("$.data[2].payload.field3", is("value3"));
    }

    @Test
    public void shouldReturnEmptyFeedIfNoData() throws IOException {
        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, randomUUID());

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        with(responseBodyOf(response))
                .assertThat("$.data", hasSize(0));
    }

    @Test
    public void shouldReturnFirstPage() throws Exception {
        final UUID streamId = randomUUID();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);

        eventFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId);

        with(responseBodyOf(response))
                .assertThat("$.data", hasSize(2));

    }

    @Test
    public void shouldFollowToThe2ndPage() throws Exception {
        final UUID streamId = randomUUID();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);

        eventFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId);

        final String responseBody = responseBodyOf(response);

        with(responseBody)
                .assertThat("$.paging.next", not(nullValue()));

        final String nextPageUrl = JsonPath.read(responseBody, "$.paging.next");

        final HttpResponse secondPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        assertThat(secondPage.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        with(responseBodyOf(secondPage))
                .assertThat("$.data", hasSize(1))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(3));
    }

    @Test
    public void shouldFollowToThe3rdPage() throws Exception {
        final UUID streamId = randomUUID();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, createObjectBuilder().add("field4", "value4").build().toString(), new UtcClock().now());
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, createObjectBuilder().add("field5", "value5").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        eventFeedService.initialiseWithPageSize(2);

        final HttpResponse firstPage = eventsFeedFor(SYSTEM_USER_ID, streamId);

        String nextPageUrl = JsonPath.read(responseBodyOf(firstPage), "$.paging.next");

        final HttpResponse secondPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        nextPageUrl = JsonPath.read(responseBodyOf(secondPage), "$.paging.next");

        final HttpResponse thirdPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        assertThat(thirdPage.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final String thirdPageBody = responseBodyOf(thirdPage);
        with(thirdPageBody)
                .assertThat("$.data", hasSize(1))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(5));
    }

    @Test
    public void shouldGoBackToThePreviousPage() throws Exception {
        final UUID streamId = randomUUID();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);

        eventFeedService.initialiseWithPageSize(2);

        final HttpResponse firstPage = eventsFeedFor(SYSTEM_USER_ID, streamId);
        String nextPageUrl = JsonPath.read(responseBodyOf(firstPage), "$.paging.next");

        assertThat(nextPageUrl, containsString("/rest/event-streams/" + streamId + "?page=2"));

        final HttpResponse secondPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        String previousPageUrl = JsonPath.read(responseBodyOf(secondPage), "$.paging.previous");
        final HttpResponse firstPageAgain = feedOf(previousPageUrl, SYSTEM_USER_ID);

        assertThat(firstPageAgain.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        with(responseBodyOf(firstPageAgain))
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(1))
                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].sequenceId", is(2));
        assertThat(previousPageUrl, containsString("/rest/event-streams/" + streamId + "?page=1"));
    }


    @Test
    public void shouldNotPresentLinkTo2ndPageIfNoMoreRecords() throws Exception {
        final UUID streamId = randomUUID();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);

        eventFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId);

        with(responseBodyOf(response))
                .assertNotDefined("$.paging.previous");

    }

    @Test
    public void shouldNotPresentLinkToPreviousPageIfOn1stPage() throws Exception {

        final UUID streamId = randomUUID();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("feild1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("feild2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("feild3", "value3").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);

        eventFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, randomUUID());

        with(responseBodyOf(response))
                .assertNotDefined("$.paging.previous");

    }

    @Test
    public void shouldReturnForbiddenIfNotASystemUser() throws IOException {
        final HttpResponse response = eventsFeedFor(randomUUID(), randomUUID());
        assertThat(response.getStatusLine().getStatusCode(), is(FORBIDDEN.getStatusCode()));
    }

    @Test
    public void shouldFollowLinksFromEventsStreamsToSpecificEventsFeed() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        eventStreamsFeedService.initialiseWithPageSize(3);

        eventStreamRepository.insert(new EventStream(streamId1));
        eventStreamRepository.insert(new EventStream(streamId2));
        eventStreamRepository.insert(new EventStream(streamId3));

        final Event event1 = new Event(randomUUID(), streamId2, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId2, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId2, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);

        eventFeedService.initialiseWithPageSize(2);

        final HttpResponse eventStreamsResponse = eventStreamsFeedFor(SYSTEM_USER_ID);

        assertThat(eventStreamsResponse.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String eventStreamId2URL = JsonPath.read(responseBodyOf(eventStreamsResponse), "$.data[1].href");

        final HttpResponse secondStream = feedOf(eventStreamId2URL, SYSTEM_USER_ID);

        assertThat(secondStream.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final String thirdPageBody = responseBodyOf(secondStream);
        with(thirdPageBody)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId2.toString()))
                .assertThat("$.data[0].sequenceId", is(1))
                .assertThat("$.data[0].payload.field1", is("value1"))
                .assertThat("$.data[1].streamId", is(streamId2.toString()))
                .assertThat("$.data[1].sequenceId", is(2))
                .assertThat("$.data[1].payload.field2", is("value2"));

    }

    private String responseBodyOf(final HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private HttpResponse eventStreamsFeedFor(final UUID userId) throws IOException {
        final String url = format(BASE_URI_PATTERN + "/event-streams", port);
        return feedOf(url, userId);
    }

    private HttpResponse eventsFeedFor(final UUID userId, final UUID streamId) throws IOException {
        final String url = format(BASE_URI_PATTERN + "/event-streams/" + streamId.toString(), port);
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

    @ApplicationScoped
    public static class TestEventInsertionStrategyProducer {

        @Produces
        public EventInsertionStrategy eventLogInsertionStrategy() {
            return new AnsiSQLEventLogInsertionStrategy();
        }
    }
}
