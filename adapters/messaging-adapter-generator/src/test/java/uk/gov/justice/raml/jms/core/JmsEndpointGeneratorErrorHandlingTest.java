package uk.gov.justice.raml.jms.core;

import static java.util.Collections.emptyMap;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.core.Generator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;


public class JmsEndpointGeneratorErrorHandlingTest {

    private static final String BASE_PACKAGE = "uk.test";
    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Generator generator = new JmsEndpointGenerator();

    @Test
    public void shouldThrowExceptionIfInvalidTierPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid uri: /structure.unknowntier.command");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.unknowntier.command")
                                .withDefaultAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfInvalidPillarPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid uri: /lifecycle.controller.unknown");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/lifecycle.controller.unknown")
                                .withDefaultAction()
                        )
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfNoPillarPassedInUri() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid uri: /structure.controller");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller")
                                .withDefaultAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfNoResourcesInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No resources specified");

        generator.run(
                raml().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfNoActionsInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No actions to process");

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Request type not set");

        generator.run(
                raml()
                        .with(resource()
                                .with(action().withActionType(POST)))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainAValidCommand() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.people.unknown.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.unknown.command1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfNotvalidMediaType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: nd.people.unknown.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action(POST, "nd.people.unknown.command1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfOneOfMediaTypesNotValid() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.people.commaods.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action()
                                        .withActionType(POST)
                                        .withMediaType("application/vnd.people.commaods.command1+json")
                                        .withMediaType("application/vnd.people.command.command1+json")
                                ))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionWhenBaseUriNotSetWhileGeneratingEventListener() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Base uri not set");

        generator.run(
                raml()
                        .withBaseUri(null)
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .withDefaultAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, emptyMap()));


    }

    @Test
    public void shouldThrowExceptionWhenInvalidBaseUriWhileGeneratingEventListener() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid base uri: message://too/short/uri");

        generator.run(
                raml()
                        .withBaseUri("message://too/short/uri")
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .withDefaultAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, emptyMap()));


    }

}
