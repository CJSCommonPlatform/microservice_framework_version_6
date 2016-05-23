package uk.gov.justice.raml.jms.validator;

import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MessagingRequestTypeRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RamlValidator validator = new MessagingRequestTypeRamlValidator();

    @Test
    public void shouldPassIfMediaTypeContainsAValidCommand() throws Exception {

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.command.command1+json")))
                        .build());

    }


    @Test
    public void shouldPassIfMediaTypeContainsAValidEvent() throws Exception {

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.event.event1+json")))
                        .build());

    }

    @Test
    public void shouldPassIfMediaTypeContainsPluralEventPillarName() throws Exception {

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.events.event1+json")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainAValidEvent() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.people.eventss.event1+json");

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.eventss.event1+json")))
                        .build());

    }


}
