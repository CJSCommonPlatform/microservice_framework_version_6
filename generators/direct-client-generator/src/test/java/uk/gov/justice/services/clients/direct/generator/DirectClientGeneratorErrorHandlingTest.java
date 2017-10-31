package uk.gov.justice.services.clients.direct.generator;
import static java.util.Collections.emptyMap;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DirectClientGeneratorErrorHandlingTest extends BaseGeneratorTest {

    @Before
    public void setUp() throws Exception {
        generator = new DirectClientGenerator();
    }
    @Test
    public void shouldThrowExceptionIfMappingInDescriptionFieldSyntacticallyIncorrect() throws Exception {
        thrown.expect(RamlValidationException.class);
        thrown.expectMessage("Invalid action mapping in RAML file");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription("........ aaa incorrect mapping")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));
    }

    @Test
    public void shouldThrowExceptionIfMappingNull() throws Exception {
        thrown.expect(RamlValidationException.class);
        thrown.expectMessage("Invalid action mapping in RAML file");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(httpAction()
                                        .withHttpActionType(GET)
                                        .withResponseTypes("application/vnd.ctx.query.defquery+json")
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));
    }

    @Test
    public void shouldThrowExceptionIfNoResourcesInRaml() throws Exception {

        thrown.expect(RamlValidationException.class);
        thrown.expectMessage("No resources specified");

        generator.run(
                raml().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfNoActionsInRaml() throws Exception {

        thrown.expect(RamlValidationException.class);
        thrown.expectMessage("No actions to process");

        generator.run(
                raml()
                        .with(resource("/path"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfResponseTypeNotSetForGETAction() throws Exception {

        thrown.expect(RamlValidationException.class);
        thrown.expectMessage("Response type not set");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/path")
                                .with(httpActionWithDefaultMapping(GET))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionForPOST() throws Exception {

        thrown.expect(RamlValidationException.class);
        thrown.expectMessage("Http action type not supported: POST");

        generator.run(
                restRamlWithDefaults().withDefaultPostResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfActionTypeIsHEAD() throws Exception {
        thrown.expect(RamlValidationException.class);
        thrown.expectMessage("Http action type not supported: HEAD");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction().withHttpActionType(HEAD))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }
}
