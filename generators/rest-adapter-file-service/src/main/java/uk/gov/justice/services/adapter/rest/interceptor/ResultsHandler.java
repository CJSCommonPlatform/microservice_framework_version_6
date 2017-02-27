package uk.gov.justice.services.adapter.rest.interceptor;

import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;

import uk.gov.justice.services.messaging.DefaultJsonEnvelope.Builder;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResultsHandler {

    public JsonEnvelope addResultsTo(final JsonEnvelope inputEnvelope, final Map<String, UUID> results) {

        final Builder builder = envelopeFrom(inputEnvelope);

        results.forEach((fieldName, fileId) -> builder.withPayloadOf(fileId.toString(), fieldName));

        return builder.build();
    }
}
