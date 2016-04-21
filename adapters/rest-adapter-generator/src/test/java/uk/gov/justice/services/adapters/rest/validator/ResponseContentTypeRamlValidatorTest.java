package uk.gov.justice.services.adapters.rest.validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

public class ResponseContentTypeRamlValidatorTest {

    private RamlValidator validator = new ResponseContentTypeRamlValidator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldPassIfResponseContentTypeContainsAValidQueryName() throws Exception {

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(action(GET).withActionWithResponseTypes("application/vnd.ctx.query.query1+json"))
                ).build());
    }

    @Test
    public void shouldIgnoreInvalidResponseContentTypeInNonGETActions() throws Exception {

        validator.validate(
                raml()
                        .with(resource("/some/path")
                                .with(action(GET).withActionWithResponseTypes("application/vnd.ctx.query.query1+json")))
                        .with(resource("/some/path")
                                .with(action(POST).withActionWithResponseTypes("application/vnd.ctx.invalid.aa+json")))
                        .build());
    }

    @Test
    public void shouldThrowExceptionIfResponseContentTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Response type not set");

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(action(GET))
                ).build());

    }

    @Test
    public void shouldThrowExceptionIfdResponseContentTypeInvalid() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/vnd.people.invalid.abc1+json");


        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(action(GET).withActionWithResponseTypes("application/vnd.people.invalid.abc1+json"))
                ).build());

    }


    @Test
    public void shouldThrowExceptionIfdResponseContentTypeDoesNotContainContext() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/vnd.people.invalid.abc1+json");


        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(action(GET).withActionWithResponseTypes("application/vnd.people.invalid.abc1+json"))
                ).build());

    }


    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainContext() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/vnd.query.query1+json");

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(action(GET).withActionWithResponseTypes("application/vnd.query.query1+json"))
                ).build());

    }

}
