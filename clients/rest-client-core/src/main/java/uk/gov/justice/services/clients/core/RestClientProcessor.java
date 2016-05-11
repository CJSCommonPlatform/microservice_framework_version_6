package uk.gov.justice.services.clients.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelper.toEnvelopeTraceString;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;
import static uk.gov.justice.services.messaging.logging.ResponseLoggerHelper.toResponseTrace;

/**
 * Helper service for processing requests for generating REST clients.
 */
public class RestClientProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientProcessor.class);

    private static final String MEDIA_TYPE_PATTERN = "application/vnd.%s+json";
    private static final String CPPID = "CPPID";

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    Enveloper enveloper;

    /**
     * Make a request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response from that the endpoint returned for this request
     */
    public JsonEnvelope request(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final Client client = ClientBuilder.newClient();

        WebTarget target = client
                .target(definition.getBaseURi())
                .path(definition.getPath());

        for (String pathParam : definition.getPathParams()) {
            target = target.resolveTemplate(pathParam, payload.getString(pathParam));
        }

        for (QueryParam queryParam : definition.getQueryParams()) {
            final String paramName = queryParam.getName();
            if (!payload.containsKey(paramName) && queryParam.isRequired()) {
                throw new IllegalStateException(format("Query parameter %s is required, but not present in envelope", paramName));
            }

            if (payload.containsKey(paramName)) {
                target = target.queryParam(paramName, payload.getString(paramName));
            }
        }

        final Invocation.Builder builder = target.request(format(MEDIA_TYPE_PATTERN, envelope.metadata().name()));

        final WebTarget finalTarget = target;
        trace(LOGGER, () -> String.format("Sending REST request to %s using message: %s", finalTarget.getUri().toString(),
                toEnvelopeTraceString(envelope)));

        final Response response = builder.get();

        trace(LOGGER, () -> String.format("REST response for %s received: %s", envelope.metadata().id().toString(), toResponseTrace(response)));

        final int status = response.getStatus();
        if (status == NOT_FOUND.getStatusCode()) {
            return enveloper.withMetadataFrom(envelope, envelope.metadata().name()).apply(null);
        } else if (status != OK.getStatusCode()) {
            throw new RuntimeException(format("Request Failed with code %s and reason \"%s\"", status,
                    response.getStatusInfo().getReasonPhrase()));
        }

        final JsonObject responseAsJsonObject = stringToJsonObjectConverter.convert(response.readEntity(String.class));

        return jsonObjectEnvelopeConverter.asEnvelope(addMetadataIfMissing(responseAsJsonObject, envelope.metadata(), response.getHeaderString(CPPID)));
    }

    private JsonObject addMetadataIfMissing(final JsonObject responseAsJsonObject, final Metadata requestMetadata, final String cppId) {
        if (responseAsJsonObject.containsKey(METADATA)) {
            return responseAsJsonObject;
        }

        final JsonObject metadata = JsonObjects.createObjectBuilderWithFilter(requestMetadata.asJsonObject(), x -> !ID.equals(x))
                .add(ID, cppId)
                .build();

        return createObjectBuilder(responseAsJsonObject)
                .add(METADATA, metadata).build();
    }

}
