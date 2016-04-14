package uk.gov.justice.raml.jms.validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;

import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

public class ResourceUriRamlValidatorTest {

    private RamlValidator validator = new ResourceUriRamlValidator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldPassIfCorrectUri() {
        validator.validate(raml().with(resource().withRelativeUri("/structure.api.commands")).build());
        validator.validate(raml().with(resource().withRelativeUri("/people.controller.commands")).build());
        validator.validate(raml().with(resource().withRelativeUri("/lifecycle.handler.commands")).build());
        validator.validate(raml().with(resource().withRelativeUri("/structure.events")).build());
    }

    @Test
    public void shouldPassIfCorrectUriInMultipleResources() {
        validator.validate(raml()
                .with(resource().withRelativeUri("/people.api.commands"))
                .with(resource().withRelativeUri("/structure.handler.commands"))
                .with(resource().withRelativeUri("/people.events"))
                .build());
    }
    
  
    @Test
    public void shouldThrowExceptionIfInvalidTierPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Inavlid uri: /structure.unknowntier.commands");

        validator.validate(raml()
                .with(resource()
                        .withRelativeUri("/structure.unknowntier.commands"))
                .build());

    }
    
    @Test
    public void shouldThrowExceptionIfOneOfResourcesContainsInvalidTierInUri() {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Inavlid uri: /people.unknown.commands");
        
        validator.validate(raml()
                .with(resource().withRelativeUri("/structure.handler.commands"))
                .with(resource().withRelativeUri("/people.unknown.commands"))
                .with(resource().withRelativeUri("/people.events"))
                .build());
    }
    
    @Test
    public void shouldThrowExceptionIfTooManyElementsInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Inavlid uri: /structure.handler.commands.abc");

        validator.validate(raml()
                .with(resource()
                        .withRelativeUri("/structure.handler.commands.abc"))
                .build());

    }



}
