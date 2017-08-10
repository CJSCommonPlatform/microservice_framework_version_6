package uk.gov.justice.services.adapter.rest.interceptor;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObjectBuilder;

@ApplicationScoped
public class ResultsHandler {

    public JsonEnvelope addResultsTo(final JsonEnvelope inputEnvelope, final Map<String, UUID> results) {

        final JsonObjectBuilder objectBuilder = createObjectBuilder(inputEnvelope.payloadAsJsonObject());

        results.forEach((fieldName, fileId) -> objectBuilder.add(fieldName, fileId.toString()));

        return envelopeFrom(inputEnvelope.metadata(), objectBuilder.build());
    }
}
