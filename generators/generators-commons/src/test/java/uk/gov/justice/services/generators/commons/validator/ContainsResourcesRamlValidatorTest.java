package uk.gov.justice.services.generators.commons.validator;


import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Raml;

public class ContainsResourcesRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RamlValidator validator = new ContainsResourcesRamlValidator();

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
