package uk.gov.justice.services.core.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.Test;

public class MediaTypesTest {

    @Test
    public void shouldCreateMediaTypesWithRequestAndResponse() {
        final MediaType request = mock(MediaType.class);
        final MediaType response = mock(MediaType.class);
        final MediaTypes mediaTypes = new MediaTypes(request, response);

        assertThat(mediaTypes.getRequestMediaType(), is(Optional.of(request)));
        assertThat(mediaTypes.getResponseMediaType(), is(Optional.of(response)));
    }

    @Test
    public void shouldCreateMediaTypesWithRequest() {
        final MediaType request = mock(MediaType.class);
        final MediaTypes mediaTypes = new MediaTypes(request, null);

        assertThat(mediaTypes.getRequestMediaType(), is(Optional.of(request)));
        assertThat(mediaTypes.getResponseMediaType(), is(Optional.empty()));
    }

    @Test
    public void shouldCreateMediaTypesWithResponse() {
        final MediaType response = mock(MediaType.class);
        final MediaTypes mediaTypes = new MediaTypes(null, response);

        assertThat(mediaTypes.getRequestMediaType(), is(Optional.empty()));
        assertThat(mediaTypes.getResponseMediaType(), is(Optional.of(response)));
    }
}