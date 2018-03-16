package uk.gov.justice.raml.jms.core;

import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.raml.jms.config.GeneratorPropertiesFactory;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.generators.commons.validator.RamlValidationException;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class JmsEndpointGeneratorErrorHandlingTest extends BaseGeneratorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void before() {
        super.before();
        generator = new JmsEndpointGenerator();
    }

    @Test
    public void shouldThrowExceptionIfNoResourcesInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No resources specified");

        generator.run(
                raml().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Request type not set");

        generator.run(
                raml()
                        .with(resource()
                                .with(httpAction().withHttpActionType(POST)))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));
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
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, new CommonGeneratorProperties()));
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
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, new CommonGeneratorProperties()));
    }

    @Test
    public void shouldThrowExceptionWhenUnsupportedFrameworkComponent() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("JMS Endpoint generation is unsupported for framework component type QUERY_API");

        generator.run(
                raml()
                        .withBaseUri("message://query/api/message/people")
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(QUERY_API)));
    }
}
