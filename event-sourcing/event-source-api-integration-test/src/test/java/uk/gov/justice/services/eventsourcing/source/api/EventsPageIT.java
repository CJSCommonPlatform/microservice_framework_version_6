package uk.gov.justice.services.eventsourcing.source.api;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
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
import static uk.gov.justice.services.core.h2.OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider.SYSTEM_USER_ID;

import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.rest.ForbiddenRequestExceptionMapper;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.cdi.InitialContextProducer;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;
import uk.gov.justice.services.eventsource.DefaultEventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.publisher.jms.JmsEventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventPageResource;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventSourceApiApplication;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsPageService;
import uk.gov.justice.services.eventsourcing.source.api.service.UrlLinkFactory;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventsService;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionFactory;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionValueFactory;
import uk.gov.justice.services.eventsourcing.source.api.util.LoggerProducer;
import uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider;
import uk.gov.justice.services.eventsourcing.source.core.EventAppender;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventSourceNameExtractor;
import uk.gov.justice.services.eventsourcing.source.core.EventSourceProducer;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManagerFactory;
import uk.gov.justice.services.eventsourcing.source.core.JdbcBasedEventSource;
import uk.gov.justice.services.eventsourcing.source.core.PublishingEventAppenderFactory;
import uk.gov.justice.services.eventsourcing.source.core.SystemEventService;
import uk.gov.justice.services.jdbc.persistence.DataSourceJndiNameProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.subscription.ParserProducer;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.registry.EventSourceRegistryProducer;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import org.apache.openejb.util.NetworkUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class EventsPageIT {


    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/event-source-api/rest";
    private static final UUID STREAM_ID = randomUUID();
    private static final String EVENT_STREAM_URL_PATH_PREFIX = "/event-source-api/rest/event-streams/" + STREAM_ID;
    private static final int PAGE_SIZE = 2;
    private static int port = -1;

    private CloseableHttpClient httpClient;

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    private EventJdbcRepository eventsRepository;

    @BeforeClass
    public static void beforeClass() throws NamingException {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Before
    public void setup() throws Exception {
        httpClient = HttpClients.createDefault();
        InitialContext initialContext = new InitialContext();
        initialContext.bind("java:/app/EventsPageIT/DS.eventstore", dataSource);
        initEventDatabase();
        eventsRepository = eventJdbcRepositoryFactory.eventJdbcRepository("java:openejb/Resource/eventStore");
    }

    @Configuration
    public Properties configuration() {
        return createOpenEjbConfigurationBuilder()
                .addInitialContext()
                .addHttpEjbPort(port)
                .addH2EventStore()
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
            EventJdbcRepositoryFactory.class,
            EventsService.class,
            AccessController.class,
            TestSystemUserProvider.class,
            ForbiddenRequestExceptionMapper.class,
            TestEventInsertionStrategyProducer.class,
            EventsPageService.class,
            LoggerProducer.class,
            PositionFactory.class,
            UrlLinkFactory.class,
            PositionValueFactory.class,
            BadRequestExceptionMapper.class,
            JdbcRepositoryHelper.class,
            UtcClock.class,
            JdbcDataSourceProvider.class,
            DefaultJsonValidationLoggerHelper.class,
            EventSource.class,
            JdbcBasedEventSource.class,
            EventAppender.class,
            PublishingEventAppenderFactory.class,
            EventRepositoryFactory.class,
            EventConverter.class,
            SystemEventService.class,
            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            GlobalValueProducer.class,
            DefaultEnveloper.class,
            EventPublisher.class,
            JmsEventPublisher.class,
            DefaultEventDestinationResolver.class,
            DefaultJsonObjectEnvelopeConverter.class,
            DefaultJmsEnvelopeSender.class,
            EnvelopeConverter.class,
            TraceLogger.class,
            DefaultTraceLogger.class,
            DefaultEnvelopeConverter.class,
            JsonObjectToObjectConverter.class,
            ObjectToJsonObjectConverter.class,

            EventSourceProducer.class,
            EventSourceNameExtractor.class,
            EventSourceRegistryProducer.class,
            ParserProducer.class,
            YamlFileFinder.class,
            YamlParser.class,
            YamlSchemaLoader.class,

            DataSourceJndiNameProvider.class,
            InitialContextProducer.class,

            EventStreamManagerFactory.class,
            EventStreamJdbcRepositoryFactory.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("event-source-api")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", EventSourceApiApplication.class.getName());
    }

    @Test
    public void shouldReturnTheFullEventInfo() throws Exception {
        storeEvents(3);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].name", containsString("name"))
                .assertThat("$.data[0].position", is(3))
                .assertThat("$.data[0].payload.field3", is("value3"))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].name", containsString("name"))
                .assertThat("$.data[1].position   ", is(2))
                .assertThat("$.data[1].payload.field2", is("value2"));
    }

    @Test
    public void shouldReturnHeadEvents() throws Exception {

        storeEvents(5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "HEAD", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page1 = responseBodyOf(response);

        with(page1)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(5))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(4));

        with(page1)
                .assertNotDefined("$.pagingLinks.next");

        final String page2Url = JsonPath.read(page1, "$.pagingLinks.previous");

        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/2"));

        assertHeadAndLastLinks(page1);
    }

    @Test
    public void shouldThrowExceptionWhenHeadRequestedNextAsDirection() throws Exception {

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "HEAD", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnLatestEvents() throws Exception {

        storeEvents(5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "5", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page1 = responseBodyOf(response);

        with(page1)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(5))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(4));

        with(page1)
                .assertNotDefined("$.pagingLinks.next");

        final String page2Url = JsonPath.read(page1, "$.pagingLinks.previous");

        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page1);
    }

    @Test
    public void shouldReturnFirstEvents() throws Exception {

        storeEvents(5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "1", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page3 = responseBodyOf(response);
        with(page3)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(2))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(1));

        final String page2Url = JsonPath.read(page3, "$.pagingLinks.next");

        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/FORWARD/" + PAGE_SIZE));

        with(page3)
                .assertNotDefined("$.pagingLinks.previous");

        assertHeadAndLastLinks(page3);
    }

    @Test
    public void shouldThrowExceptionWhenFirstRequestedPreviousAsDirection() throws Exception {

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "1", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnOlderEventsPreviousAndNextLinks() throws Exception {

        storeEvents(5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page2 = responseBodyOf(response);
        with(page2)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(3))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(2));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.next");

        assertThat(page1Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/4/FORWARD/" + PAGE_SIZE));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.previous");

        assertThat(page3Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page2);
    }

    @Test
    public void shouldReturnNewerEventsPreviousAndNextLinks() throws Exception {

        storeEvents(5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page2 = responseBodyOf(response);
        with(page2)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(4))

                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(3));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.next");

        assertThat(page1Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/5/FORWARD/" + PAGE_SIZE));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.previous");

        assertThat(page3Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/2/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page2);
    }

    @Test
    public void shouldNotReturnRecordsForUnknownPosition() throws Exception {

        storeEvents(5);

        final HttpResponse response = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "6", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(0));

        assertHeadAndLastLinks(value);
    }

    @Test
    public void shouldReturnEmptyFeedIfNoDataAndNoPreviousAndNextLinks() throws IOException {

        eventsRepository.findAll(); // Hit the repo to initialise.

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
    public void shouldGoPage2FromHead() throws Exception {

        storeEvents(5);

        final HttpResponse firstPageResponse = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "HEAD", BACKWARD, PAGE_SIZE);

        assertThat(firstPageResponse.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String firstPage = responseBodyOf(firstPageResponse);

        with(firstPage)
                .assertNotDefined("$.pagingLinks.next");

        final String page2Url = JsonPath.read(firstPage, "$.pagingLinks.previous");

        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        final HttpResponse page2HttpResponse = feedOf(page2Url, SYSTEM_USER_ID);

        final String page2 = responseBodyOf(page2HttpResponse);

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(3))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(2));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.next");
        assertThat(page3Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/4/FORWARD/" + PAGE_SIZE));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.previous");
        assertThat(page1Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page2);
    }

    @Test
    public void shouldGoToPage3FromPage2() throws Exception {

        storeEvents(5);

        final HttpResponse secondPageResponse = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", BACKWARD, PAGE_SIZE);

        final String page2 = responseBodyOf(secondPageResponse);

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(3))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(2));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.previous");

        final HttpResponse page3HttpResponse = feedOf(page3Url, SYSTEM_USER_ID);

        final String page3 = responseBodyOf(page3HttpResponse);

        with(page3)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(2))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(1));

        with(page3)
                .assertNotDefined("$.pagingLinks.previous");

        assertHeadAndLastLinks(page3);

        final String page2Url = JsonPath.read(page3, "$.pagingLinks.next");
        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/FORWARD/" + PAGE_SIZE));
    }

    @Test
    public void shouldGoToPage1FromPage2() throws Exception {

        storeEvents(5);

        final HttpResponse secondPageResponse = eventsFeedFor(SYSTEM_USER_ID, STREAM_ID, "3", BACKWARD, PAGE_SIZE);

        final String page2 = responseBodyOf(secondPageResponse);

        with(page2)
                .assertThat("$.data", hasSize(PAGE_SIZE))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(3))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(PAGE_SIZE));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.next");

        final HttpResponse page1HttpResponse = feedOf(page1Url, SYSTEM_USER_ID);

        final String page1 = responseBodyOf(page1HttpResponse);

        with(page1)
                .assertThat("$.data", hasSize(PAGE_SIZE))
                .assertThat("$.data[0].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[0].position", is(5))
                .assertThat("$.data[1].streamId", is(STREAM_ID.toString()))
                .assertThat("$.data[1].position", is(4));

        with(page1)
                .assertNotDefined("$.pagingLinks.next");

        final String page2Url = JsonPath.read(page1, "$.pagingLinks.previous");
        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

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

    private void storeEvents(final int eventNumber) throws InvalidPositionException {
        for (int i = 1; i <= eventNumber; i++) {
            final UUID id = randomUUID();

            final Metadata metadata = JsonEnvelope.metadataBuilder()
                    .withId(id)
                    .withStreamId(STREAM_ID)
                    .withName("name")
                    .createdAt(now())
                    .withVersion(i)
                    .build();

            final Event event = new Event(randomUUID(), STREAM_ID, valueOf(i), "Test Name" + i,
                    metadata.asJsonObject().toString(), createObjectBuilder().add("field" + i, "value" + i).build().toString(),
                    new UtcClock().now(), "source");
            eventsRepository.insert(event);
        }
    }

    private void assertHeadAndLastLinks(String value) {
        final String headUrl = JsonPath.read(value, "$.pagingLinks.head");
        assertThat(headUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/HEAD/BACKWARD/" + PAGE_SIZE));

        final String firstUrl = JsonPath.read(value, "$.pagingLinks.first");
        assertThat(firstUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));
    }


}
