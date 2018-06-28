package uk.gov.justice.services.example.cakeshop.event.listener.replay;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.sourcing.subscription.EventReplayer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

public class CakeEventReplayer implements EventReplayer {

    @Override
    public void replay(final InterceptorChainProcessor interceptorChainProcessor) {
        final JsonEnvelope jsonEnvelope = generateJsonEnvelope(randomUUID(), "example.recipe-added", 1L);

        interceptorChainProcessor.process(InterceptorContext.interceptorContextWithInput(jsonEnvelope));
    }

    private JsonEnvelope generateJsonEnvelope(final UUID streamId, final String eventName, final long version) {
        return envelopeFrom(
                metadataBuilder()
                        .withId(randomUUID())
                        .withName(eventName)
                        .withStreamId(streamId)
                        .withVersion(version)
                        .withSource("example"),
                createObjectBuilder()
                        .add("recipeId", randomUUID().toString())
                        .add("name", "Carrot Cake")
                        .add("glutenFree", false)
                        .add("ingredients", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("name", "carrot")
                                        .add("quantity", 1)
                                ).build())
                        .build()
        );
    }
}
