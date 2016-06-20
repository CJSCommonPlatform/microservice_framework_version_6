package uk.gov.justice.raml.jms.validator;

import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResourceUriRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RamlValidator validator = new ResourceUriRamlValidator();

    @Test
    public void shouldPassIfCorrectUri() {
        validator.validate(raml().with(resource().withRelativeUri("/structure.api.command")).build());
        validator.validate(raml().with(resource().withRelativeUri("/people.controller.command")).build());
        validator.validate(raml().with(resource().withRelativeUri("/lifecycle.handler.command")).build());
        validator.validate(raml().with(resource().withRelativeUri("/structure.event")).build());
    }

    @Test
    public void shouldPassIfCorrectUriInMultipleResources() {
        validator.validate(raml()
                .with(resource().withRelativeUri("/people.api.command"))
                .with(resource().withRelativeUri("/structure.handler.command"))
                .with(resource().withRelativeUri("/people.event"))
                .build());
    }


    @Test
    public void shouldThrowExceptionIfInvalidTierPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid uri: /structure.unknowntier.command");

        validator.validate(raml()
                .with(resource()
                        .withRelativeUri("/structure.unknowntier.command"))
                .build());

    }

    @Test
    public void shouldThrowExceptionIfOneOfResourcesContainsInvalidTierInUri() {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid uri: /people.unknown.command");

        validator.validate(raml()
                .with(resource().withRelativeUri("/structure.handler.command"))
                .with(resource().withRelativeUri("/people.unknown.command"))
                .with(resource().withRelativeUri("/people.event"))
                .build());
    }

    @Test
    public void shouldThrowExceptionIfTooManyElementsInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid uri: /structure.handler.command.abc");

        validator.validate(raml()
                .with(resource()
                        .withRelativeUri("/structure.handler.command.abc"))
                .build());

    }


}
