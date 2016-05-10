package uk.gov.justice.services.adapters.rest.validator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResponseContentTypeRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RamlValidator validator = new ResponseContentTypeRamlValidator();

    @Test
    public void shouldPassIfResponseContentTypeContainsAVendorSpecificJsonMediaType() throws Exception {

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(httpAction(GET).withResponseTypes("application/vnd.user+json"))
                ).build());
    }

    @Test
    public void shouldIgnoreInvalidResponseContentTypeInNonGETActions() throws Exception {

        validator.validate(
                raml()
                        .with(resource("/some/path")
                                .with(httpAction(GET).withResponseTypes("application/xml")))
                        .with(resource("/some/path")
                                .with(httpAction(POST).withResponseTypes("application/json")))
                        .build());
    }

    @Test
    public void shouldThrowExceptionIfResponseContentTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Response type not set");

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(httpAction(GET))
                ).build());

    }

    @Test
    public void shouldThrowExceptionIfdResponseNotVendorSpecific() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/json");


        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(httpAction(GET).withResponseTypes("application/json"))
                ).build());

    }


    @Test
    public void shouldThrowExceptionIfdResponseContentTypeDoesNotContainFormat() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/vnd.blah");


        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(httpAction(GET).withResponseTypes("application/vnd.blah"))
                ).build());

    }


}
