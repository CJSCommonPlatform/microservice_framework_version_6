package uk.gov.justice.raml.jms.validation;

import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Raml;

public class ContainsResourcesRamlValidatorTest {

    private RamlValidator validator = new ContainsResourcesRamlValidator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfNoResourcesInRaml() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("No resources specified");

        validator.validate(new Raml());

    }

    @Test
    public void shouldPassIfThereIsAResourceDefinedInRaml() {
        validator.validate(raml().with(resource()).build());
    }

}
