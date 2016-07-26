package uk.gov.justice.services.generators.commons.validator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RequestContentTypeRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldPassIfMediaTypeContainsAValidCommand() throws Exception {

        new RequestContentTypeRamlValidator(false).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/vnd.command1+json")))
                        .build());

        new RequestContentTypeRamlValidator(true).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/vnd.command1+json")))
                        .build());

    }

    @Test
    public void shouldIgnoreInvalidMediaTypesInNonPOSTActions() throws Exception {

        new RequestContentTypeRamlValidator(false).validate(
                raml()
                        .with(resource()
                                .with(httpAction(GET, "application/vnd.command1+json"))
                                .with(httpAction(POST, "application/vnd.command2+json"))
                                .with(httpAction(HEAD, "application/vnd.command3+json"))
                                .with(httpAction(PUT, "application/vnd.command4+json"))
                                .with(httpAction(OPTIONS, "application/vnd.command5+json"))
                        )
                        .build());

        new RequestContentTypeRamlValidator(true).validate(
                raml()
                        .with(resource()
                                .with(httpAction(GET, "application/vnd.command1+json"))
                                .with(httpAction(POST, "application/vnd.command2+json"))
                                .with(httpAction(HEAD, "application/vnd.command3+json"))
                                .with(httpAction(PUT, "application/vnd.command4+json"))
                                .with(httpAction(OPTIONS, "application/vnd.command5+json"))
                        )
                        .build());
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Request type not set");

        new RequestContentTypeRamlValidator(false).validate(raml()
                .with(resource()
                        .with(httpAction().withHttpActionType(POST)))
                .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainFormat() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.command1");

        new RequestContentTypeRamlValidator(false).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/vnd.command1")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainFormat2() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.command1");

        new RequestContentTypeRamlValidator(true).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/vnd.command1")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfNotAVendorMediaType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/json");

        new RequestContentTypeRamlValidator(false).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/json")))
                        .build());

    }

    @Test
    public void shouldPassValidationWhenGeneralJsonTypeAllowed() throws Exception {

        new RequestContentTypeRamlValidator(true).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/json")))
                        .build());

    }


    @Test
    public void shouldThrowExceptionIfNotValidMediaType2() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: nd.command1+nonjson");

        new RequestContentTypeRamlValidator(false).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "nd.command1+nonjson")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionForXMLMediaType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.command1+xml");

        new RequestContentTypeRamlValidator(false).validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/vnd.command1+xml")))
                        .build());

    }

}
