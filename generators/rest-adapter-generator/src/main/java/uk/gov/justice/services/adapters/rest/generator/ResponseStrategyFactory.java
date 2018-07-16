package uk.gov.justice.services.adapters.rest.generator;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.ACCEPTED_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.ACCEPTED_STATUS_NO_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.FILE_STREAM_RETURNING_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.OK_STATUS_ENVELOPE_PAYLOAD_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSynchronousAction;

import java.util.Optional;

import org.raml.model.Action;
import org.raml.model.MimeType;

public class ResponseStrategyFactory {

    public String nameOfResponseStrategyFor(final Action action, final Optional<String> component) {

        if (containsOctetStreamResponse(action)) {
            return FILE_STREAM_RETURNING_RESPONSE_STRATEGY;
        }

        if (!isSynchronousAction(action)) {

            if (containsResponse(action)) {
                return ACCEPTED_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;
            }

            return ACCEPTED_STATUS_NO_ENTITY_RESPONSE_STRATEGY;
        }

        if (isPresentAndRequiresEnvelopeEntityResponse(component)) {
            return OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;
        }

        return OK_STATUS_ENVELOPE_PAYLOAD_ENTITY_RESPONSE_STRATEGY;
    }

    private boolean containsOctetStreamResponse(final Action action) {
        for (final org.raml.model.Response response : action.getResponses().values()) {
            if (response.getBody() != null) {
                for (final MimeType mimeType : response.getBody().values()) {
                    if (mimeType.getType().equals(APPLICATION_OCTET_STREAM)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isPresentAndRequiresEnvelopeEntityResponse(final Optional<String> component) {
        return component.isPresent() && (QUERY_CONTROLLER.equals(component.get()) || QUERY_VIEW.equals(component.get()));
    }

    private boolean containsResponse(final Action action) {
        for (final org.raml.model.Response response : action.getResponses().values()) {
            if (response.getBody() != null) {
                return true;
            }
        }
        return false;
    }
}
