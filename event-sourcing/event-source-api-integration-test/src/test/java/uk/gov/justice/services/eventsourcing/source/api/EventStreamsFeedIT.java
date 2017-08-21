package uk.gov.justice.services.eventsourcing.source.api;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider.SYSTEM_USER_ID;

import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.common.rest.ForbiddenRequestExceptionMapper;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventSourceApiApplication;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventStreamsFeedResource;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.util.OpenEjbAwareEventStreamRepository;
import uk.gov.justice.services.eventsourcing.source.api.util.TestEventStreamsFeedService;
import uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
public class EventStreamsFeedIT {
    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final String BASE_URI_PATTERN = "http://localhost:%d/event-source-api/rest";
    private static int port = -1;

    private CloseableHttpClient httpClient;

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

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
            EventStreamsFeedResource.class,
            OpenEjbAwareEventStreamRepository.class,
            TestEventStreamsFeedService.class,
            AccessController.class,
            TestSystemUserProvider.class,
            ForbiddenRequestExceptionMapper.class,

    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("event-source-api")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", EventSourceApiApplication.class.getName());
    }

    @Before
    public void setUp() throws Exception {
        eventStreamsFeedService.initialiseWithPageSize(25);
    }

    @Test
    public void shouldReturnFirstPageOfFeed() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        eventStreamsFeedService.initialiseWithPageSize(3);

        eventStreamRepository.insert(new EventStream(streamId1));
        eventStreamRepository.insert(new EventStream(streamId2));
        eventStreamRepository.insert(new EventStream(streamId3));
        eventStreamRepository.insert(new EventStream(randomUUID()));

        final HttpResponse response = eventStreamsFeedFor(SYSTEM_USER_ID);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        with(responseBodyOf(response))
                .assertThat("$.data", hasSize(3))
                .assertThat("$.data[0].streamId", is(streamId1.toString()))
                .assertThat("$.data[0].href", containsString(streamId1.toString()))
                .assertThat("$.data[1].streamId", is(streamId2.toString()))
                .assertThat("$.data[1].href", containsString(streamId2.toString()))
                .assertThat("$.data[2].streamId", is(streamId3.toString()))
                .assertThat("$.data[2].href", containsString(streamId3.toString()));

    }

    @Test
    public void shouldReturnEmptyFeedIfNoData() throws IOException {
        final HttpResponse response = eventStreamsFeedFor(SYSTEM_USER_ID);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        with(responseBodyOf(response))
                .assertThat("$.data", hasSize(0));
    }

    @Test
    public void shouldReturnFirstPage() throws Exception {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        eventStreamRepository.insert(new EventStream(streamId1));
        eventStreamRepository.insert(new EventStream(streamId2));
        eventStreamRepository.insert(new EventStream(streamId3));

        eventStreamsFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventStreamsFeedFor(SYSTEM_USER_ID);

        with(responseBodyOf(response))
                .assertThat("$.data", hasSize(2));

    }

    @Test
    public void shouldFollowToThe2ndPage() throws Exception {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        eventStreamRepository.insert(new EventStream(streamId1));
        eventStreamRepository.insert(new EventStream(streamId2));
        eventStreamRepository.insert(new EventStream(streamId3));

        eventStreamsFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventStreamsFeedFor(SYSTEM_USER_ID);

        final String responseBody = responseBodyOf(response);


        with(responseBody)
                .assertThat("$.paging.next", not(nullValue()));

        final String nextPageUrl = JsonPath.read(responseBody, "$.paging.next");

        final HttpResponse secondPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        assertThat(secondPage.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        with(responseBodyOf(secondPage))
                .assertThat("$.data", hasSize(1))
                .assertThat("$.data[0].streamId", is(streamId3.toString()))
                .assertThat("$.data[0].href", containsString(streamId3.toString()));

    }

    @Test
    public void shouldFollowToThe3rdPage() throws Exception {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();


        eventStreamRepository.insert(new EventStream(streamId1));
        eventStreamRepository.insert(new EventStream(streamId2));
        eventStreamRepository.insert(new EventStream(streamId3));
        eventStreamRepository.insert(new EventStream(streamId4));
        eventStreamRepository.insert(new EventStream(streamId5));

        eventStreamsFeedService.initialiseWithPageSize(2);

        final HttpResponse firstPage = eventStreamsFeedFor(SYSTEM_USER_ID);


        String nextPageUrl = JsonPath.read(responseBodyOf(firstPage), "$.paging.next");

        final HttpResponse secondPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        nextPageUrl = JsonPath.read(responseBodyOf(secondPage), "$.paging.next");

        final HttpResponse thirdPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        assertThat(thirdPage.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final String thirdPageBody = responseBodyOf(thirdPage);
        with(thirdPageBody)
                .assertThat("$.data", hasSize(1))
                .assertThat("$.data[0].streamId", is(streamId5.toString()));
    }

    @Test
    public void shouldGoBackToThePreviousPage() throws IOException {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();


        eventStreamRepository.insert(new EventStream(streamId1));
        eventStreamRepository.insert(new EventStream(streamId2));
        eventStreamRepository.insert(new EventStream(streamId3));

        eventStreamsFeedService.initialiseWithPageSize(2);

        final HttpResponse firstPage = eventStreamsFeedFor(SYSTEM_USER_ID);
        String nextPageUrl = JsonPath.read(responseBodyOf(firstPage), "$.paging.next");

        final HttpResponse secondPage = feedOf(nextPageUrl, SYSTEM_USER_ID);

        String previousPageUrl = JsonPath.read(responseBodyOf(secondPage), "$.paging.previous");
        final HttpResponse firstPageAgain = feedOf(previousPageUrl, SYSTEM_USER_ID);

        assertThat(firstPageAgain.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        with(responseBodyOf(firstPageAgain))
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].streamId", is(streamId1.toString()))
                .assertThat("$.data[1].streamId", is(streamId2.toString()));


    }

    @Test
    public void shouldNotPresentLinkTo2ndPageIfNoMoreRecords() throws Exception {

        eventStreamRepository.insert(new EventStream(randomUUID()));
        eventStreamRepository.insert(new EventStream(randomUUID()));

        eventStreamsFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventStreamsFeedFor(SYSTEM_USER_ID);

        final String responseBody = responseBodyOf(response);


        with(responseBody)
                .assertNotDefined("$.paging.next");

    }

    @Test
    public void shouldNotPresentLinkToPreviousPageIfOn1stPage() throws Exception {

        eventStreamRepository.insert(new EventStream(randomUUID()));
        eventStreamRepository.insert(new EventStream(randomUUID()));
        eventStreamRepository.insert(new EventStream(randomUUID()));

        eventStreamsFeedService.initialiseWithPageSize(2);

        final HttpResponse response = eventStreamsFeedFor(SYSTEM_USER_ID);

        final String responseBody = responseBodyOf(response);

        with(responseBody)
                .assertNotDefined("$.paging.previous");

    }

    @Test
    public void shouldReturnForbiddenIfNotASystemUser() throws IOException {
        final HttpResponse response = eventStreamsFeedFor(randomUUID());
        assertThat(response.getStatusLine().getStatusCode(), is(FORBIDDEN.getStatusCode()));
    }

    private String responseBodyOf(final HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private HttpResponse eventStreamsFeedFor(final UUID userId) throws IOException {
        final String url = format(BASE_URI_PATTERN + "/event-streams", port);
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
}
