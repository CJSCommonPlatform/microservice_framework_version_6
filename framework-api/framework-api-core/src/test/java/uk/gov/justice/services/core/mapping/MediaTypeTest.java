package uk.gov.justice.services.core.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class MediaTypeTest {

    @Test
    public void shouldCreateAMediaTypeFromTypeAndSubType() throws Exception {

        final MediaType mediaType = new MediaType("application", "vnd.starship.command.commence-formation-attack+json");

        assertThat(mediaType.toString(), is("application/vnd.starship.command.commence-formation-attack+json"));
    }

    @Test
    public void shouldCreateAMediaTypeFromAMediaTypeString() throws Exception {

        final MediaType mediaType = new MediaType("application/vnd.starship.command.commence-formation-attack+json");

        assertThat(mediaType.getType(), is("application"));
        assertThat(mediaType.getSubtype(), is("vnd.starship.command.commence-formation-attack+json"));
    }

    @Test
    public void shouldFailIfTheMediaTypeStringIsMalformed() throws Exception {
        try {
            new MediaType("vnd.some-malformed+json");
            fail();
        } catch (final MalformedMediaTypeException expected) {
            assertThat(expected.getMessage(), is("Cannot parse media type 'vnd.some-malformed+json' into type and subtype. Missing slash character"));
        }
    }

    @Test
    public void shouldHaveCorrectEqualsAndHashcode() {
        final MediaType mediaType_1 = new MediaType("application", "vnd.starship.command.commence-formation-attack+json");
        final MediaType mediaType_2 = new MediaType("application", "vnd.starship.command.commence-formation-attack+json");
        final MediaType mediaType_3 = new MediaType("other", "vnd.starship.command.commence-formation-attack+json");
        final MediaType mediaType_4 = new MediaType("application", "vnd.other.command.commence-other+json");

        new EqualsTester()
                .addEqualityGroup(mediaType_1, mediaType_2)
                .addEqualityGroup(mediaType_3)
                .addEqualityGroup(mediaType_4)
                .testEquals();
    }
}
