package uk.gov.justice.services.core.audit;

import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.core.configuration.ValueProducer;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.function.Function;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

/**
 * Sends audit commands to the Audit context in an asynchronous manner.
 */
@ApplicationScoped
@Alternative
@Priority(5)
public class RemoteAuditClient implements AuditClient {

    private static final String RECORD_AUDIT_COMMAND = "audit.record-entry";
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";
    private static final String ORIGIN = "origin";

    @Inject
    Logger logger;

    @Inject
    Requester requester;

    @Inject
    Enveloper enveloper;

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    @Override
    public void auditEntry(final JsonEnvelope envelope) {
        try {
            requester.request(createOutgoingEnvelopeFrom(envelope));
        } catch (Exception e) {
            logger.error(String.format("Failed to audit entry for %s", envelope.toString()), e);
        }
    }

    private JsonEnvelope createOutgoingEnvelopeFrom(final JsonEnvelope envelope) {

        final JsonObject jsonObject = envelope.payloadAsJsonObject();
        final String message = jsonObject.getString("message");
        final JsonEnvelope auditPayloadEnvelope = envelope()
                .withPayloadOf(ZonedDateTimes.toString(ZonedDateTime.now()), TIMESTAMP)
                .withPayloadOf(serviceContextNameProvider.getServiceContextName(), ORIGIN)
                .withPayloadOf(message, MESSAGE)
                .build();

        final Function<Object, JsonEnvelope> function = enveloper.withMetadataFrom(envelope, RECORD_AUDIT_COMMAND);

        return function.apply(auditPayloadEnvelope.payload());
    }
}
