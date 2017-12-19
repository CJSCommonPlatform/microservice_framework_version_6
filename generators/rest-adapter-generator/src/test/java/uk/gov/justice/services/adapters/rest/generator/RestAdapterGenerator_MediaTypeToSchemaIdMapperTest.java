package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.VALID_JSON_SCHEMA;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;

import java.util.Map;

import org.junit.Test;
import org.raml.model.MimeType;

public class RestAdapterGenerator_MediaTypeToSchemaIdMapperTest extends BaseRestAdapterGeneratorTest {

    private static final MediaType MEDIA_TYPE_1 = new MediaType("application/vnd.ctx.command.command1+json");

    @Test
    public void shouldGenerateMediaTypeToSchemaIdMapper() throws Exception {
        final String schemaId = "http://justice.gov.uk/test/schema.json";
        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString(), schemaId);

        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("contextA.someAction")
                                                .withResponseType(MEDIA_TYPE_1.toString()))
                                        .withResponseTypes(mimeType_1)
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

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
