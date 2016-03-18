package uk.gov.justice.raml.jms.validation;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.ActionType;

public class MediaTypeRamlValidatorTest {

    private RamlValidator validator = new MediaTypeRamlValidator();

    @Test
    public void shouldPassIfMediaTypeContainsAValidCommand() throws Exception {

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.commands.command1+json")))
                        .build());

    }
    
    @Test
    public void shouldIgnoreInvalidMediaTypesInNonPOSTActions() throws Exception {

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(GET, "application/vnd.structure.dummy.command1+json"))
                                .with(action(POST, "application/vnd.structure.commands.command2+json"))
                                .with(action(HEAD, "application/vnd.structure.dummy.command3+json"))
                                .with(action(PUT, "application/vnd.structure.dummy.command4+json"))
                                .with(action(OPTIONS, "application/vnd.structure.dummy.command5+json"))
                                )
                        .build());

    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfMediaTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No declared media types");

        validator.validate(raml()
                .with(resource()
                        .with(action().with(ActionType.POST)))
                .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainAValidCommand() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid media type: application/vnd.people.invalid.command1+json");

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.invalid.command1+json")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfNotvalidMediaType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid media type: nd.people.commands.command1+json");

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "nd.people.commands.command1+json")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfNotvalidMediaType2() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid media type: nd.people.unknown.command1+nonjson");

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "nd.people.unknown.command1+nonjson")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainContext() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid media type: application/vnd.handlers.command1+json");

        validator.validate(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.handlers.command1+json")))
                        .build());

    }

}
