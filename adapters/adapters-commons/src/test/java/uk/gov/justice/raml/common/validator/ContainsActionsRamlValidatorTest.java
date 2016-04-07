package uk.gov.justice.raml.common.validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

public class ContainsActionsRamlValidatorTest {

    private RamlValidator validator = new ContainsActionsRamlValidator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldPassIfThereIsAnActionInRaml() {
        validator.validate(raml().with(resource().with(action())).build());
    }
    
    @Test
    public void shouldThrowExceptionIfNoActionsInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No actions to process");

        validator.validate(raml().with(resource()).build());

    }

}
