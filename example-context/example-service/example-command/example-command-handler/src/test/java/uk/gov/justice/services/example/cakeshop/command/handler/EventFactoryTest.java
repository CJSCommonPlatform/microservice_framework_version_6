package uk.gov.justice.services.example.cakeshop.command.handler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.example.cakeshop.domain.event.CakeOrdered;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class EventFactoryTest {

    private EventFactory eventFactory;

    @Before
    public void setUp() throws Exception {
        eventFactory = new EventFactory();
        eventFactory.objectMapper = new ObjectMapperProducer().objectMapper();
    }

    @Test
    public void shouldCreateCakeOrderedEvent() throws Exception {

        final CakeOrdered cakeOrdered = eventFactory.cakeOrderedEventFrom(envelope().with(metadataWithDefaults())
                .withPayloadOf("163af847-effb-46a9-96bc-32a0f7526f22", "orderId")
                .withPayloadOf("163af847-effb-46a9-96bc-32a0f7526f23", "recipeId")
                .withPayloadOf("163af847-effb-46a9-96bc-32a0f7526f22", "orderId")
                .withPayloadOf("2016-01-14T22:15:03.000000123+04:00", "deliveryDate").build());

        assertThat(cakeOrdered.getOrderId(), is(UUID.fromString("163af847-effb-46a9-96bc-32a0f7526f22")));
        assertThat(cakeOrdered.getRecipeId(), is(UUID.fromString("163af847-effb-46a9-96bc-32a0f7526f23")));
        assertThat(cakeOrdered.getDeliveryDate(), is(ZonedDateTime.of(2016, 01, 14, 18, 15, 3, 123, ZoneId.of("UTC"))));

    }
}
