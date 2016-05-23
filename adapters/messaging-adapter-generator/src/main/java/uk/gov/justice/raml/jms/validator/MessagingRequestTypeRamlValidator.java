package uk.gov.justice.raml.jms.validator;

import uk.gov.justice.raml.common.validator.RequestContentTypeRamlValidator;

import org.raml.model.ActionType;

/**
 * Checks if action's mediaType is valid and contains a valid command or event.
 * Relaxes event type validation to accept plural to ease context migration to new framework.
 *
 */
public class MessagingRequestTypeRamlValidator extends RequestContentTypeRamlValidator {

    public MessagingRequestTypeRamlValidator() {
        super(ActionType.POST, "request type", "command", "event", "events");
    }
}
