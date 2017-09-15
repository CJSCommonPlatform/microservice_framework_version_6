package uk.gov.justice.services.eventsourcing.source.api;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider.SYSTEM_USER_ID;
import static uk.gov.justice.services.jdbc.persistence.Link.HEAD;
import static uk.gov.justice.services.jdbc.persistence.Link.NEXT;
import static uk.gov.justice.services.jdbc.persistence.Link.PREVIOUS;

import uk.gov.justice.services.common.rest.ForbiddenRequestExceptionMapper;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
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
import uk.gov.justice.services.jdbc.persistence.Link;

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
                .initialiseWithPageSize();
    }

    @Test
    public void shouldReturnHeadOrLatestEvents() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event5CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), event5CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 0L, HEAD, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name5"))
                .assertThat("$.data[0].sequenceId", is(5))
                .assertThat("$.data[0].createdAt", is(event5CreatedAt.toString()))
                .assertThat("$.data[0].payload.field5", is("value5"))

                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].name", containsString("Test Name4"))
                .assertThat("$.data[1].sequenceId", is(4))
                .assertThat("$.data[1].createdAt", is(event4CreatedAt.toString()))
                .assertThat("$.data[1].payload.field4", is("value4"));


    }


    @Test
    public void shouldNotPresentPreviousWhenHeadIsRequested() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event5CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), event5CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 0L, HEAD, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        with(value)
                .assertNotDefined("$.paging.previous");

        final String nextPageUrl = JsonPath.read(value, "$.paging.next");
        assertThat(nextPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/3/NEXT/2"));

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }

    @Test
    public void shouldReturnlastOrOldestEvents() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 0L, Link.LAST, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name1"))
                .assertThat("$.data[0].sequenceId", is(1))
                .assertThat("$.data[0].createdAt", is(event1CreatedAt.toString()))
                .assertThat("$.data[0].payload.field1", is("value1"))

                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].name", containsString("Test Name2"))
                .assertThat("$.data[1].sequenceId", is(2))
                .assertThat("$.data[1].createdAt", is(event2CreatedAt.toString()))
                .assertThat("$.data[1].payload.field2", is("value2"));
    }

    @Test
    public void shouldNotReturnNextPageLinnkForLastOrOldestEvents() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 0L, Link.LAST, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        final String previousPageUrl = JsonPath.read(value, "$.paging.previous");

        assertThat(previousPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/3/PREVIOUS/2"));

        with(value)
                .assertNotDefined("$.paging.next");

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }

    @Test
    public void shouldReturnEventsBackwardsFromSequenceId5() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event5CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), event5CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 5L, NEXT, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name5"))
                .assertThat("$.data[0].sequenceId", is(5))
                .assertThat("$.data[0].createdAt", is(event5CreatedAt.toString()))
                .assertThat("$.data[0].payload.field5", is("value5"))

                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].name", containsString("Test Name4"))
                .assertThat("$.data[1].sequenceId", is(4))
                .assertThat("$.data[1].createdAt", is(event4CreatedAt.toString()))
                .assertThat("$.data[1].payload.field4", is("value4"));
    }

    @Test
    public void shouldReturnCorrectNextAndPreviousLinksWhenEventsBackwardsFromSequenceId5IsRequested() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event5CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), event5CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 5L, NEXT, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        with(value)
                .assertNotDefined("$.paging.previous");

        final String nextPageUrl = JsonPath.read(value, "$.paging.next");

        assertThat(nextPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/3/NEXT/2"));

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }


    @Test
    public void shouldReturnEventsBackwardsFromSequenceId3() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event3CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), event3CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 3l, NEXT, 2L);

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
    public void shouldReturnCorrectNextAndPreviousLinksWhenEventsBackwardsFromSequenceId3IsRequested() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event3CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), event3CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 3l, NEXT, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        final String previousPageUrl = JsonPath.read(value, "$.paging.previous");

        assertThat(previousPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/4/PREVIOUS/2"));

        final String nextPageUrl = JsonPath.read(value, "$.paging.next");

        assertThat(nextPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/1/NEXT/2"));

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));

    }

    @Test
    public void shouldReturnEventsBackwardsFromSequenceId1() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 1l, NEXT, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(1))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name1"))
                .assertThat("$.data[0].sequenceId", is(1))
                .assertThat("$.data[0].createdAt", is(event1CreatedAt.toString()))
                .assertThat("$.data[0].payload.field1", is("value1"));
    }

    @Test
    public void shouldReturnCorrectNextAndPreviousLinksWhenEventsBackwardsFromSequenceId1IsRequested() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 1l, NEXT, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        final String previousPageUrl = JsonPath.read(value, "$.paging.previous");

        assertThat(previousPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/2/PREVIOUS/2"));

        with(value)
                .assertNotDefined("$.paging.next");

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }


    @Test
    public void shouldReturnEventsForwardFromSequenceId1() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();


        final ZonedDateTime event1CreatedAt = new UtcClock().now();

        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 1l, PREVIOUS, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name2"))
                .assertThat("$.data[0].sequenceId", is(2))
                .assertThat("$.data[0].createdAt", is(event2CreatedAt.toString()))
                .assertThat("$.data[0].payload.field2", is("value2"))

                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].name", containsString("Test Name1"))
                .assertThat("$.data[1].sequenceId", is(1))
                .assertThat("$.data[1].createdAt", is(event1CreatedAt.toString()))
                .assertThat("$.data[1].payload.field1", is("value1"));
    }

    @Test
    public void shouldReturnCorrectNextAndPreviousLinksWhenEventsForwardFromSequenceId1IsRequested() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();


        final ZonedDateTime event1CreatedAt = new UtcClock().now();

        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 1l, PREVIOUS, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        final String previousPageUrl = JsonPath.read(value, "$.paging.previous");

        assertThat(previousPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/3/PREVIOUS/2"));

        with(value)
                .assertNotDefined("$.paging.next");

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }

    @Test
    public void shouldReturnEventsForwardFromSequenceId3() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();


        final ZonedDateTime event3CreatedAt = new UtcClock().now();

        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), event3CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 3l, PREVIOUS, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name4"))
                .assertThat("$.data[0].sequenceId", is(4))
                .assertThat("$.data[0].createdAt", is(event4CreatedAt.toString()))
                .assertThat("$.data[0].payload.field4", is("value4"))

                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].name", containsString("Test Name3"))
                .assertThat("$.data[1].sequenceId", is(3))
                .assertThat("$.data[1].createdAt", is(event3CreatedAt.toString()))
                .assertThat("$.data[1].payload.field3", is("value3"));


    }

    @Test
    public void shouldReturnCorrectNextAndPreviousLinksWhenEventsForwardFromSequenceId3IsRequested() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event3CreatedAt = new UtcClock().now();

        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), event3CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 3l, PREVIOUS, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        final String previousPageUrl = JsonPath.read(value, "$.paging.previous");

        assertThat(previousPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/5/PREVIOUS/2"));

        final String nextPageUrl = JsonPath.read(value, "$.paging.next");

        assertThat(nextPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/2/NEXT/2"));

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }


    @Test
    public void shouldReturnEventsForwardFromSequenceId5() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();


        final ZonedDateTime event5CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), event5CreatedAt);

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 5l, PREVIOUS, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(1))

                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].name", containsString("Test Name5"))
                .assertThat("$.data[0].sequenceId", is(5))
                .assertThat("$.data[0].createdAt", is(event5CreatedAt.toString()))
                .assertThat("$.data[0].payload.field5", is("value5"));
    }

    @Test
    public void shouldReturnCorrectNextAndPreviousLinksWhenEventsForwardFromSequenceId5IsRequested() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();


        final ZonedDateTime event5CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), event5CreatedAt);

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 5l, PREVIOUS, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        final String nextPageUrl = JsonPath.read(value, "$.paging.next");

        assertThat(nextPageUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/4/NEXT/2"));

        with(value)
                .assertNotDefined("$.paging.previous");

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }


    @Test
    public void shouldNotReturnRecordsForUnknownSequenceId() throws Exception {

        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();


        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 6l, PREVIOUS, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(0));

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));

    }

    @Test
    public void shouldReturnEmptyFeedIfNoData() throws IOException {
        final UUID streamId = randomUUID();
        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, streamId, 0l, NEXT, 2L);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(0));

        final String headUrl = JsonPath.read(value, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(value, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }

    @Test
    public void shouldGoNextFromPage2FromHead() throws Exception {
        final UUID streamId = randomUUID();

        eventFeedService.initialiseWithPageSize();

        final ZonedDateTime event5CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent5 = createObjectBuilder().add("field5", "value5").build();
        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();

        final Event event5 = new Event(randomUUID(), streamId, 5L, "Test Name5", METADATA_JSON, payloadEvent5.toString(), event5CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), event4CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());

        eventsRepository.insert(event1);
        eventsRepository.insert(event2);
        eventsRepository.insert(event3);
        eventsRepository.insert(event4);
        eventsRepository.insert(event5);

        final HttpResponse firstPageResponse = eventsFeedFor(SYSTEM_USER_ID, streamId, 0L, HEAD, 2L);

        assertThat(firstPageResponse.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String firstPage = responseBodyOf(firstPageResponse);

        with(firstPage)
                .assertNotDefined("$.paging.previous");

        final String nextPageUrlOfFirstPage = JsonPath.read(firstPage, "$.paging.next");

        assertThat(nextPageUrlOfFirstPage, containsString("/event-source-api/rest/event-streams/" + streamId + "/3/NEXT/2"));

        final HttpResponse nextPageResponse = feedOf(nextPageUrlOfFirstPage, SYSTEM_USER_ID);

        final String nextPage = responseBodyOf(nextPageResponse);

        with(nextPage)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].sequenceId", is(2));

        final String previousPageUrlOfNextPage = JsonPath.read(nextPage, "$.paging.previous");
        assertThat(previousPageUrlOfNextPage, containsString("/event-source-api/rest/event-streams/" + streamId + "/4/PREVIOUS/2"));

        final String nextPageUrlOfNextPage = JsonPath.read(nextPage, "$.paging.next");
        assertThat(nextPageUrlOfNextPage, containsString("/event-source-api/rest/event-streams/" + streamId + "/1/NEXT/2"));

        final String headUrl = JsonPath.read(nextPage, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(nextPage, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));

    }


    @Test
    public void shouldGoNextToPage3FromPage2() throws Exception {
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

        eventFeedService.initialiseWithPageSize();

        final HttpResponse secondPageResponse = eventsFeedFor(SYSTEM_USER_ID, streamId, 3L, NEXT, 2L);

        final String page2 = responseBodyOf(secondPageResponse);

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].sequenceId", is(2));

        final String page3Url = JsonPath.read(page2, "$.paging.next");

        final HttpResponse nextPageResponse = feedOf(page3Url, SYSTEM_USER_ID);

        final String page3 = responseBodyOf(nextPageResponse);

        with(page3)
                .assertThat("$.data", hasSize(1))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(1));

        with(page3)
                .assertNotDefined("$.paging.next");
        final String previousPageUrlOfNextPage = JsonPath.read(page3, "$.paging.previous");
        assertThat(previousPageUrlOfNextPage, containsString("/event-source-api/rest/event-streams/" + streamId + "/2/PREVIOUS/2"));

        final String headUrl = JsonPath.read(page3, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(page3, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));

    }

    @Test
    public void shouldGoPreviousPage2FromPage3() throws Exception {
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

        eventFeedService.initialiseWithPageSize();

        final HttpResponse secondPageResponse = eventsFeedFor(SYSTEM_USER_ID, streamId, 1L, NEXT, 2L);

        final String page3 = responseBodyOf(secondPageResponse);

        with(page3)
                .assertThat("$.data", hasSize(1))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(1));

        final String page2Url = JsonPath.read(page3, "$.paging.previous");

        final HttpResponse previousPageResponse = feedOf(page2Url, SYSTEM_USER_ID);

        final String page2 = responseBodyOf(previousPageResponse);

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].sequenceId", is(2));
        ;


        final String page2PreviousUrl = JsonPath.read(page2, "$.paging.previous");
        assertThat(page2PreviousUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/4/PREVIOUS/2"));

        final String page2NextUrl = JsonPath.read(page2, "$.paging.next");
        assertThat(page2NextUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/1/NEXT/2"));

        final String headUrl = JsonPath.read(page2, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(page2, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));

    }

    @Test
    public void shouldGoPreviousPage1FromPage2() throws Exception {
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

        eventFeedService.initialiseWithPageSize();

        final HttpResponse secondPageResponse = eventsFeedFor(SYSTEM_USER_ID, streamId, 3L, NEXT, 2L);

        final String page2 = responseBodyOf(secondPageResponse);

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(3))
                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].sequenceId", is(2));

        final String page2Url = JsonPath.read(page2, "$.paging.previous");

        final HttpResponse previousPageResponse = feedOf(page2Url, SYSTEM_USER_ID);

        final String page1 = responseBodyOf(previousPageResponse);

        with(page1)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId.toString()))
                .assertThat("$.data[0].sequenceId", is(5))
                .assertThat("$.data[1].streamId", is(streamId.toString()))
                .assertThat("$.data[1].sequenceId", is(4));
        ;

        with(page1)
                .assertNotDefined("$.paging.previous");

        final String page2NextUrl = JsonPath.read(page1, "$.paging.next");
        assertThat(page2NextUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/3/NEXT/2"));

        final String headUrl = JsonPath.read(page1, "$.paging.head");
        assertThat(headUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/HEAD/2"));

        final String lastUrl = JsonPath.read(page1, "$.paging.last");
        assertThat(lastUrl, containsString("/event-source-api/rest/event-streams/" + streamId + "/0/LAST/2"));
    }

    @Test
    public void shouldReturnForbiddenIfNotASystemUser() throws IOException {
        final HttpResponse response = eventsFeedFor(randomUUID(), randomUUID(), 0l, NEXT, 2L);
        assertThat(response.getStatusLine().getStatusCode(), is(FORBIDDEN.getStatusCode()));
    }

    private String responseBodyOf(final HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private HttpResponse eventsFeedFor(final UUID userId, final UUID streamId, final long offset, final Link link, final long pageSize) throws IOException {
        final String url = format(BASE_URI_PATTERN + "/event-streams/" + streamId.toString() + "/" + offset + "/" + link
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

    @ApplicationScoped
    public static class TestEventInsertionStrategyProducer {

        @Produces
        public EventInsertionStrategy eventLogInsertionStrategy() {
            return new AnsiSQLEventLogInsertionStrategy();
        }
    }
}
