package uk.gov.justice.services.adapters.rest.generator;


import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.MAPPING_BOUNDARY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.MAPPING_SEPARATOR;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.NAME_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.REQUEST_TYPE_KEY;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Raml;

public class RestAdapterGenerator_ActionMapperErrorHandlingTest extends BaseRestAdapterGeneratorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfMappingInDescriptionFieldSyntacticallyIncorrect() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid action mapping in RAML file");

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
    public void shouldThrowExceptionIfMappingEmpty() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid action mapping in RAML file");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription("...\n...\n")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));
    }

    @Test
    public void shouldThrowExceptionIfMappingNull() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid action mapping in RAML file");

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
    public void shouldThrowExceptionIfNameNotInMapping() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Action name not defined in mapping");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription(MAPPING_BOUNDARY + "\n" +
                                                MAPPING_SEPARATOR + "\n" +
                                                REQUEST_TYPE_KEY + ": application/vnd.structure.command.test-cmd+json\n" +
                                                MAPPING_BOUNDARY + "\n")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));
    }

    @Test
    public void shouldThrowExceptionIfNoMediaTypeSetInMapping() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type not defined in mapping");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription(MAPPING_BOUNDARY + "\n" +
                                                MAPPING_SEPARATOR + "\n" +
                                                NAME_KEY + ": nameABC\n" +
                                                MAPPING_BOUNDARY + "\n")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotMappedInPOSTHttpAction() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type(s) not mapped to an action: [application/vnd.ctx.command.somemediatype2+json]");


        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpActionWithDefaultMapping(POST)
                                        .with(mapping()
                                                .withName("contextC.someAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype1+json"))
                                        .withMediaType("application/vnd.ctx.command.somemediatype1+json", "json/schema/somemediatype1.json")
                                        .withMediaType("application/vnd.ctx.command.somemediatype2+json", "json/schema/somemediatype2.json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotMappedInGETHttpAction() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type(s) not mapped to an action: [application/vnd.mediatype3+json]");

        generator.run(
                restRamlWithDefaults().with(
                        resource("/user")
                                .with(httpActionWithDefaultMapping(GET)
                                        .with(mapping()
                                                .withName("contextA.someAction")
                                                .withResponseType("application/vnd.mediatype1+json"))
                                        .with(mapping()
                                                .withName("contextA.someOtherAction")
                                                .withResponseType("application/vnd.mediatype2+json"))
                                        .withResponseTypes(
                                                "application/vnd.mediatype1+json",
                                                "application/vnd.mediatype2+json",
                                                "application/vnd.mediatype3+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));
    }

    @Test
    public void shouldThrowExceptionIfActionTypeIsHEAD() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Http Method of type HEAD is not supported by the Action Mapper");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpActionWithDefaultMapping(HEAD, "application/vnd.default+json"))
                ).build();

        new ActionMappingGenerator().generateFor(raml);
    }

    @Test
    public void shouldThrowExceptionIfActionTypeIsOPTIONS() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Http Method of type OPTIONS is not supported by the Action Mapper");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpActionWithDefaultMapping(OPTIONS, "application/vnd.default+json"))
                ).build();

        new ActionMappingGenerator().generateFor(raml);
    }

    @Test
    public void shouldThrowExceptionIfActionTypeIsTRACE() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Http Method of type TRACE is not supported by the Action Mapper");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpActionWithDefaultMapping(TRACE, "application/vnd.default+json"))
                ).build();

        new ActionMappingGenerator().generateFor(raml);
    }

}
