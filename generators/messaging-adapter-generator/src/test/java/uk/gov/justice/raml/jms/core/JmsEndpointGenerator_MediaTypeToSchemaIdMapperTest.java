package uk.gov.justice.raml.jms.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.SCHEMA_ID;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.raml.jms.config.GeneratorPropertiesFactory;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.raml.model.Raml;

public class JmsEndpointGenerator_MediaTypeToSchemaIdMapperTest {

    private static final MediaType MEDIA_TYPE_1 = new MediaType("application/vnd.ctx.command.command1+json");
    private static final String BASE_PACKAGE = "uk.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JavaCompilerUtil compiler;
    private Generator<Raml> generator = new JmsEndpointGenerator();

    @Before
    public void setup() {
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldGenerateMediaTypeToSchemaIdMapper() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .with(httpAction(POST, MEDIA_TYPE_1.toString())))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(COMMAND_CONTROLLER)));

        final Class<?> schemaIdMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "ProcessorMediaTypeToSchemaIdMapper");
        final MediaTypeToSchemaIdMapper instance = (MediaTypeToSchemaIdMapper) schemaIdMapperClass.newInstance();

        final Map<MediaType, String> mediaTypeToSchemaIdMap = instance.getMediaTypeToSchemaIdMap();

        assertThat(mediaTypeToSchemaIdMap.size(), is(1));
        assertThat(mediaTypeToSchemaIdMap.get(MEDIA_TYPE_1), is(SCHEMA_ID));
    }
}
