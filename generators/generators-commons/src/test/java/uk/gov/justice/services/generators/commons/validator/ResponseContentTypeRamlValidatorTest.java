package uk.gov.justice.services.generators.commons.validator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResponseContentTypeRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RamlValidator validator = new ResponseContentTypeRamlValidator(GET);

    @Test
    public void shouldPassIfResponseContentTypeContainsAVendorSpecificJsonMediaType() throws Exception {

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(httpActionWithDefaultMapping(GET).withResponseTypes("application/vnd.user+json"))
                ).build());
    }

    @Test
    public void shouldIgnoreInvalidResponseContentTypeInNonGETActions() throws Exception {

        validator.validate(
                raml()
                        .with(resource("/some/path")
                                .with(httpActionWithDefaultMapping(GET).withResponseTypes("application/xml")))
                        .with(resource("/some/path")
                                .with(httpActionWithDefaultMapping(POST).withResponseTypes("application/json")))
                        .build());
    }

    @Test
    public void shouldThrowExceptionIfResponseContentTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Response type not set");

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(httpActionWithDefaultMapping(GET))
                ).build());
    }
}
