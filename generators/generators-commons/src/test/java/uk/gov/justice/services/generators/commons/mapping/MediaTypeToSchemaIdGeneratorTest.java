package uk.gov.justice.services.generators.commons.mapping;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.VALID_JSON_SCHEMA;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.raml.model.MimeType;

public class MediaTypeToSchemaIdGeneratorTest {

    private static final MediaType MEDIA_TYPE_1 = new MediaType("application/vnd.ctx.command.command1+json");
    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    private JavaCompilerUtil compiler;

    @Before
    public void before() {
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldCreateMediaTypeToSchemaIdMapperForGivenRamlWithPost() throws Exception {
        final String schemaId = "http://justice.gov.uk/test/schema.json";
        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString(), schemaId);

        new MediaTypeToSchemaIdGenerator().generateMediaTypeToSchemaIdMapper(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(POST)
                                        .withMediaTypeWithDefaultSchema(mimeType_1)
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> schemaIdMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "WarnameMediaTypeToSchemaIdMapper");
        final MediaTypeToSchemaIdMapper instance = (MediaTypeToSchemaIdMapper) schemaIdMapperClass.newInstance();

        final Map<MediaType, String> mediaTypeToSchemaIdMap = instance.getMediaTypeToSchemaIdMap();

        assertThat(mediaTypeToSchemaIdMap.size(), is(1));
        assertThat(mediaTypeToSchemaIdMap.get(MEDIA_TYPE_1), is(schemaId));
    }

    @Test
    public void shouldCreateMediaTypeToSchemaIdMapperForGivenRamlWithGet() throws Exception {
        final String schemaId = "http://justice.gov.uk/test/schema.json";
        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString(), schemaId);

        new MediaTypeToSchemaIdGenerator().generateMediaTypeToSchemaIdMapper(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(GET)
                                        .withResponseTypes(mimeType_1)
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> schemaIdMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "WarnameMediaTypeToSchemaIdMapper");
        final MediaTypeToSchemaIdMapper instance = (MediaTypeToSchemaIdMapper) schemaIdMapperClass.newInstance();

        final Map<MediaType, String> mediaTypeToSchemaIdMap = instance.getMediaTypeToSchemaIdMap();

        assertThat(mediaTypeToSchemaIdMap.size(), is(1));
        assertThat(mediaTypeToSchemaIdMap.get(MEDIA_TYPE_1), is(schemaId));
    }

    private MimeType createMimeTypeWith(final String type, final String schemaId) {
        final MimeType mimeType = new MimeType();
        mimeType.setType(type);
        mimeType.setSchema(format(VALID_JSON_SCHEMA, schemaId));

        return mimeType;
    }
}