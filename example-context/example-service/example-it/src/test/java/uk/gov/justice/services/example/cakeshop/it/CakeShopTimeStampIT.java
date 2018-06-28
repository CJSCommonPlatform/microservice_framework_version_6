package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.exparity.hamcrest.date.ZonedDateTimeMatchers.within;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.ORDER_CAKE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.ORDERS_RESOURCE_URI;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.isStatus;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.example.cakeshop.it.helpers.ApiResponse;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.Querier;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;

import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CakeShopTimeStampIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private Client client;
    private Querier querier;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CAKE_SHOP_REPOSITORY_MANAGER.initialise();
    }

    @Before
    public void before() throws Exception {
        client = new RestEasyClientFactory().createResteasyClient();
        querier = new Querier(client);
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldReturnOrderWithUTCOrderDate() {
        final UUID orderId = randomUUID();

        final Response commandResponse =
                client.target(ORDERS_RESOURCE_URI + orderId.toString()).request()
                        .post(entity(
                                createObjectBuilder()
                                        .add("recipeId", randomUUID().toString())
                                        .add("deliveryDate", "2016-01-21T23:42:03.522+07:00")
                                        .build().toString(),
                                ORDER_CAKE_MEDIA_TYPE));

        assertThat(commandResponse.getStatus(), isStatus(ACCEPTED));

        await().until(() -> querier.queryForOrder(orderId.toString()).httpCode() == OK.getStatusCode());

        final ApiResponse queryResponse = querier.queryForOrder(orderId.toString());

        with(queryResponse.body())
                .assertThat("$.orderId", equalTo(orderId.toString()))
                .assertThat("$.deliveryDate", equalTo("2016-01-21T16:42:03.522Z"));
    }

    @Test
    public void shouldSetDateCreatedTimestampInEventStore() {
        final UUID orderId = randomUUID();

        final Response commandResponse =
                client.target(ORDERS_RESOURCE_URI + orderId.toString()).request()
                        .post(entity(
                                createObjectBuilder()
                                        .add("recipeId", randomUUID().toString())
                                        .add("deliveryDate", "2016-01-21T23:42:03.522+07:00")
                                        .build().toString(),
                                ORDER_CAKE_MEDIA_TYPE));

        assertThat(commandResponse.getStatus(), isStatus(ACCEPTED));

        await().until(() -> querier.queryForOrder(orderId.toString()).httpCode() == OK.getStatusCode());

        final Stream<Event> events = CAKE_SHOP_REPOSITORY_MANAGER.getEventJdbcRepository().findByStreamIdOrderByPositionAsc(orderId);
        final Event event = events.findFirst().get();

        assertThat(event.getCreatedAt(), is(notNullValue()));
        assertThat(event.getCreatedAt(), is(within(10L, SECONDS, new UtcClock().now())));
    }
}
