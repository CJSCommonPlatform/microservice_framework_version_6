package uk.gov.justice.services.generators.commons.validator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class SupportedActionTypesRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldNotPassIfActionTypeSuported() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Http action type not supported: POST");

        new SupportedActionTypesRamlValidator(GET)
                .validate(raml().with(resource().withDefaultPostAction()).build());
    }

    @Test
    public void shouldPassIfActionTypeSuported() throws Exception {

        new SupportedActionTypesRamlValidator(GET, POST)
                .validate(raml().with(resource().withDefaultPostAction()).build());
    }


}