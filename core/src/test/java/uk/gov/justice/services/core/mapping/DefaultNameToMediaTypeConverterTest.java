package uk.gov.justice.services.core.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNameToMediaTypeConverterTest {

    @InjectMocks
    private DefaultNameToMediaTypeConverter nameToMediaTypeConverter;

    @Test
    public void shouldConvertNameToMediaType() throws Exception {

        final String name = "example.command.travel-through-wormhole";

        final MediaType mediaType = nameToMediaTypeConverter.convert(name);

        assertThat(mediaType.getType(), is("application"));
        assertThat(mediaType.getSubtype(), is("vnd.example.command.travel-through-wormhole+json"));
    }

    @Test
    public void shouldConvertMediaTypeToName() throws Exception {

        final MediaType mediaType = new MediaType(
                "application",
                "vnd.example.command.travel-through-wormhole+json");

        final String name = nameToMediaTypeConverter.convert(mediaType);

        assertThat(name, is("example.command.travel-through-wormhole"));
    }

    @Test
    public void shouldFailIfSubtypeIsMalformed() throws Exception {

        final MediaType mediaType = new MediaType(
                "application",
                "json");

        try {
            nameToMediaTypeConverter.convert(mediaType);
            fail();
        } catch (final MalformedMediaTypeNameException expected) {
            assertThat(expected.getMessage(), is("Failed to extract Name from media type 'application/json'"));
        }
    }
}
