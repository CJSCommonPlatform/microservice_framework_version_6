package uk.gov.justice.services.example.cakeshop.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.matchers.EventStreamMatcher.eventStreamAppendedWith;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.example.cakeshop.domain.event.CakeOrdered;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrderCakeCommandHandlerTest {

    private static final String COMMAND_NAME = "example.order-cake";
    private static final String EVENT_NAME = "example.cake-ordered";
    private static final UUID RECIPE_ID = randomUUID();
    private static final UUID ORDER_ID = randomUUID();

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private EventFactory eventFactory;

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(CakeOrdered.class);

    @InjectMocks
    private OrderCakeCommandHandler orderCakeCommandHandler;

    @Test
    public void shouldHaveCorrectHandlesAnnotation() throws Exception {
        assertThat(orderCakeCommandHandler, isHandler(COMMAND_HANDLER)
                .with(method("handle").thatHandles(COMMAND_NAME)));
    }

    @Test
    public void shouldHandleOrderCakeCommand() throws Exception {
        when(eventSource.getStreamById(ORDER_ID)).thenReturn(eventStream);

        final String deliveryDate = "2017-01-18T15:30:20.340Z";

        final JsonEnvelope command = envelope()
                .with(metadataWithRandomUUID(COMMAND_NAME))
                .withPayloadOf(ORDER_ID, "orderId")
                .withPayloadOf(RECIPE_ID, "recipeId")
                .withPayloadOf(deliveryDate, "deliveryDate")
                .build();

        final CakeOrdered cakeOrdered = new CakeOrdered(ORDER_ID, RECIPE_ID, ZonedDateTime.parse(deliveryDate));
        when(eventFactory.cakeOrderedEventFrom(command)).thenReturn(cakeOrdered);
        orderCakeCommandHandler.handle(command);


        assertThat(eventStream, eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                withMetadataEnvelopedFrom(command)
                                        .withName(EVENT_NAME),
                                payloadIsJson(allOf(
                                        withJsonPath("$.orderId", equalTo(ORDER_ID.toString())),
                                        withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
                                        withJsonPath("$.deliveryDate", equalTo(deliveryDate))
                                ))))));
    }
}
