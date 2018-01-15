package uk.gov.justice.services.generators.commons.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import uk.gov.justice.services.core.mapping.MediaType;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class MediaTypeToSchemaIdTest {

    @Test
    public void shouldCreateMediaTypeToSchemaId() {
        final MediaType mediaType = new MediaType("application/vnd.test.command.test+json");
        final String schemaId = "schemaId";

        final MediaTypeToSchemaId mediaTypeToSchemaId = new MediaTypeToSchemaId(mediaType, schemaId);

        assertThat(mediaTypeToSchemaId.getMediaType(), is(mediaType));
        assertThat(mediaTypeToSchemaId.getSchemaId(), is(schemaId));
    }

    @Test
    public void shouldHaveCorrectEqualsAndHashcode() {
        final MediaType mediaType_1 = new MediaType("application/vnd.test.command.test+json");
        final MediaType mediaType_2 = new MediaType("application/vnd.other.command.test+json");
        final String schemaId_1 = "schemaId";
        final String schemaId_2 = "otherId";

        final MediaTypeToSchemaId mediaTypeToSchemaId_1 = new MediaTypeToSchemaId(mediaType_1, schemaId_1);
        final MediaTypeToSchemaId mediaTypeToSchemaId_2 = new MediaTypeToSchemaId(mediaType_1, schemaId_1);
        final MediaTypeToSchemaId mediaTypeToSchemaId_3 = new MediaTypeToSchemaId(mediaType_1, schemaId_2);
        final MediaTypeToSchemaId mediaTypeToSchemaId_4 = new MediaTypeToSchemaId(mediaType_2, schemaId_1);
        final MediaTypeToSchemaId mediaTypeToSchemaId_5 = new MediaTypeToSchemaId(mediaType_2, schemaId_2);

        new EqualsTester()
                .addEqualityGroup(mediaTypeToSchemaId_1, mediaTypeToSchemaId_2)
                .addEqualityGroup(mediaTypeToSchemaId_3)
                .addEqualityGroup(mediaTypeToSchemaId_4)
                .addEqualityGroup(mediaTypeToSchemaId_5)
                .testEquals();
    }

}