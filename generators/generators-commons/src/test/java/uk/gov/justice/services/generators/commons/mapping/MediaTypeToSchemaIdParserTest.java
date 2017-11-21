package uk.gov.justice.services.generators.commons.mapping;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.services.core.mapping.MediaType;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.MimeType;

@RunWith(MockitoJUnitRunner.class)
public class MediaTypeToSchemaIdParserTest {

    private static final String SCHEMA_ID_1 = "http://justice.gov.uk/example/command/api/command1.json";
    private static final String SCHEMA_ID_2 = "http://justice.gov.uk/example/command/api/command2.json";
    private static final String SCHEMA_ID_3 = "http://justice.gov.uk/example/command/api/command3.json";
    private static final String SCHEMA_ID_4 = "http://justice.gov.uk/example/command/api/command4.json";
    private static final String SCHEMA_ID_5 = "http://justice.gov.uk/example/command/api/command5.json";

    private static final MediaType MEDIA_TYPE_1 = new MediaType("application/vnd.ctx.command.command1+json");
    private static final MediaType MEDIA_TYPE_2 = new MediaType("application/vnd.ctx.command.command2+json");
    private static final MediaType MEDIA_TYPE_3 = new MediaType("application/vnd.ctx.command.command3+json");
    private static final MediaType MEDIA_TYPE_4 = new MediaType("application/vnd.ctx.command.command4+json");
    private static final MediaType MEDIA_TYPE_5 = new MediaType("application/vnd.ctx.command.command5+json");

    @Mock
    private SchemaIdParser schemaIdParser;

    @InjectMocks
    private MediaTypeToSchemaIdParser mediaTypeToSchemaIdParser;

    @Test
    public void shouldProduceListOfMediaTypeToSchemaId() throws Exception {

        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString());
        final MimeType mimeType_2 = createMimeTypeWith(MEDIA_TYPE_2.toString());
        final MimeType mimeType_3 = createMimeTypeWith(MEDIA_TYPE_3.toString());
        final MimeType mimeType_4 = createMimeTypeWith(MEDIA_TYPE_4.toString());

        when(schemaIdParser.schemaIdFrom(mimeType_1)).thenReturn(Optional.of(SCHEMA_ID_1));
        when(schemaIdParser.schemaIdFrom(mimeType_2)).thenReturn(Optional.of(SCHEMA_ID_2));
        when(schemaIdParser.schemaIdFrom(mimeType_3)).thenReturn(Optional.of(SCHEMA_ID_3));
        when(schemaIdParser.schemaIdFrom(mimeType_4)).thenReturn(Optional.of(SCHEMA_ID_4));

        final List<MediaTypeToSchemaId> mediaTypeToSchemaIds = mediaTypeToSchemaIdParser.parseFrom(restRamlWithQueryApiDefaults()
                .with(resource("/user")
                        .with(httpActionWithDefaultMapping(POST)
                                .withMediaTypeWithDefaultSchema(mimeType_1)
                                .withMediaTypeWithDefaultSchema(mimeType_2))
                        .with(httpActionWithDefaultMapping(PUT)
                                .withMediaTypeWithDefaultSchema(mimeType_3))
                        .with(httpActionWithDefaultMapping(PATCH)
                                .withMediaTypeWithDefaultSchema(mimeType_4))
                ).build());

        assertThat(mediaTypeToSchemaIds, hasItems(
                new MediaTypeToSchemaId(MEDIA_TYPE_1, SCHEMA_ID_1),
                new MediaTypeToSchemaId(MEDIA_TYPE_2, SCHEMA_ID_2),
                new MediaTypeToSchemaId(MEDIA_TYPE_3, SCHEMA_ID_3),
                new MediaTypeToSchemaId(MEDIA_TYPE_4, SCHEMA_ID_4)
        ));
    }

    @Test
    public void shouldProduceListOfMediaTypeForGet() throws Exception {

        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString());
        final MimeType mimeType_5 = createMimeTypeWith(MEDIA_TYPE_5.toString());

        when(schemaIdParser.schemaIdFrom(mimeType_1)).thenReturn(Optional.of(SCHEMA_ID_1));
        when(schemaIdParser.schemaIdFrom(mimeType_5)).thenReturn(Optional.of(SCHEMA_ID_5));

        final List<MediaTypeToSchemaId> mediaTypeToSchemaIds = mediaTypeToSchemaIdParser.parseFrom(restRamlWithQueryApiDefaults()
                .with(resource("/user")
                        .with(httpActionWithDefaultMapping(GET)
                                .withResponseTypes(mimeType_1, mimeType_5))
                ).build());

        assertThat(mediaTypeToSchemaIds, hasItems(
                new MediaTypeToSchemaId(MEDIA_TYPE_1, SCHEMA_ID_1),
                new MediaTypeToSchemaId(MEDIA_TYPE_5, SCHEMA_ID_5)
        ));
    }

    @Test
    public void shouldProduceListOfMediaTypeToSchemaIdAndIgnoreEmptySchemaIds() throws Exception {

        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString());
        final MimeType mimeType_2 = createMimeTypeWith(MEDIA_TYPE_2.toString());
        final MimeType mimeType_3 = createMimeTypeWith(MEDIA_TYPE_3.toString());

        when(schemaIdParser.schemaIdFrom(mimeType_1)).thenReturn(Optional.of(SCHEMA_ID_1));
        when(schemaIdParser.schemaIdFrom(mimeType_2)).thenReturn(Optional.empty());
        when(schemaIdParser.schemaIdFrom(mimeType_3)).thenReturn(Optional.of(SCHEMA_ID_2));

        final List<MediaTypeToSchemaId> mediaTypeToSchemaIds = mediaTypeToSchemaIdParser.parseFrom(restRamlWithQueryApiDefaults()
                .with(resource("/user")
                        .with(httpActionWithDefaultMapping(POST)
                                .withMediaTypeWithDefaultSchema(mimeType_1)
                                .withMediaTypeWithDefaultSchema(mimeType_2)
                                .withMediaTypeWithDefaultSchema(mimeType_3)
                        )
                ).build());

        assertThat(mediaTypeToSchemaIds, hasItems(
                new MediaTypeToSchemaId(MEDIA_TYPE_1, SCHEMA_ID_1),
                new MediaTypeToSchemaId(MEDIA_TYPE_3, SCHEMA_ID_2)
        ));
    }

    @Test
    public void shouldNotProduceListOfMediaTypeForUnsuportedActionType() throws Exception {

        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString());
        final MimeType mimeType_5 = createMimeTypeWith(MEDIA_TYPE_5.toString());

        when(schemaIdParser.schemaIdFrom(mimeType_1)).thenReturn(Optional.of(SCHEMA_ID_1));
        when(schemaIdParser.schemaIdFrom(mimeType_5)).thenReturn(Optional.of(SCHEMA_ID_5));

        final List<MediaTypeToSchemaId> mediaTypeToSchemaIds = mediaTypeToSchemaIdParser.parseFrom(restRamlWithQueryApiDefaults()
                .with(resource("/user")
                        .with(httpActionWithDefaultMapping(HEAD)
                                .withResponseTypes(mimeType_1, mimeType_5))
                ).build());

        assertThat(mediaTypeToSchemaIds.size(), is(0));
    }

    private MimeType createMimeTypeWith(final String type) {
        final MimeType mimeType = new MimeType();
        mimeType.setType(type);
        mimeType.setSchema("{}");

        return mimeType;
    }
}
