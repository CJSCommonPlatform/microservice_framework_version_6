package uk.gov.justice.services.clients.core.webclient;

import static java.lang.String.format;

import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.QueryParam;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class WebTargetFactory {

    @Inject
    public BaseUriFactory baseUriFactory;

    public WebTarget createWebTarget(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final Client client = ClientBuilder.newClient();

        WebTarget target = client
                .target(baseUriFactory.createBaseUri(definition))
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
                switch (queryParam.getType()) {
                    case NUMERIC:
                        target = target.queryParam(paramName, payload.getJsonNumber(paramName));
                        break;
                    case BOOLEAN:
                        target = target.queryParam(paramName, payload.getBoolean(paramName));
                        break;
                    default:
                        target = target.queryParam(paramName, payload.getString(paramName));
                }
            }
        }
        return target;
    }
}
