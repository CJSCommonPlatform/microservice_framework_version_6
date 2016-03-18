package uk.gov.justice.raml.jms.core;

import static org.raml.model.ActionType.POST;
import static uk.gov.justice.raml.jms.core.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.raml.model.ActionType;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.jms.validation.RamlValidationException;

public class JmsEndpointGeneratorErrorHandlingTest {

    private static final String BASE_PACKAGE = "uk.test";

    private Generator generator = new JmsEndpointGenerator();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfInvalidTierPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Inavlid uri: /structure.unknowntier.commands");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.unknowntier.commands")
                                .withDefaultAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
    }

    @Test
    public void shouldThrowExceptionIfInvalidPillarPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Inavlid uri: /lifecycle.controller.unknown");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/lifecycle.controller.unknown")
                                .withDefaultAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
    }

    @Test
    public void shouldThrowExceptionIfNoPillarPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Inavlid uri: /structure.controller");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller")
                                .withDefaultAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
    }

    @Test
    public void shouldThrowExceptionIfNoResourcesInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No resources specified");

        generator.run(
                raml().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfNoActionsInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No actions to process");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller.commands"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No declared media types");

        generator.run(
                raml()
                        .with(resource()
                                .with(action().with(ActionType.POST)))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainAValidCommand() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid media type: application/vnd.people.unknown.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.unknown.command1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfNotvalidMediaType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid media type: nd.people.unknown.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action(POST, "nd.people.unknown.command1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfOneOfMediaTypesNotValid() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid media type: application/vnd.people.commaods.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action()
                                        .with(ActionType.POST)
                                        .withMediaType("application/vnd.people.commaods.command1+json")
                                        .withMediaType("application/vnd.people.commands.command1+json")
                                        ))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

}
