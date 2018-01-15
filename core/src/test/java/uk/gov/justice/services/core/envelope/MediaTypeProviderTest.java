package uk.gov.justice.services.core.envelope;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypes;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MediaTypeProviderTest {

    @Mock
    private MediaTypesMappingCache mediaTypesMappingCache;

    @InjectMocks
    private MediaTypeProvider mediaTypeProvider;

    @Test
    public void shouldGetTheRequestMediaTypeForTheActionName() throws Exception {

        final String actionName = "example.add-recipe";
        final MediaType requestMediaType = new MediaType("application/vnd.example.add-recipe+json");

        final MediaTypes mediaTypes = mock(MediaTypes.class);

        when(mediaTypesMappingCache.mediaTypesFor(actionName)).thenReturn(of(mediaTypes));
        when(mediaTypes.getRequestMediaType()).thenReturn(of(requestMediaType));

        assertThat(mediaTypeProvider.getRequestMediaType(actionName), is(of(requestMediaType)));
    }

    @Test
    public void shouldGetTheResponseMediaTypeForTheActionName() throws Exception {

        final String actionName = "example.add-recipe";
        final MediaType responseMediaType = new MediaType("application/vnd.example.recipe-added+json");

        final MediaTypes mediaTypes = mock(MediaTypes.class);

        when(mediaTypesMappingCache.mediaTypesFor(actionName)).thenReturn(of(mediaTypes));
        when(mediaTypes.getResponseMediaType()).thenReturn(of(responseMediaType));

        assertThat(mediaTypeProvider.getResponseMediaType(actionName), is(of(responseMediaType)));
    }

    @Test
    public void shouldReturnEmptyIfNoMediaTypesFoundForTheActionName() throws Exception {

        final String actionName = "example.add-recipe";

        when(mediaTypesMappingCache.mediaTypesFor(actionName)).thenReturn(empty());

        assertThat(mediaTypeProvider.getRequestMediaType(actionName), is(empty()));
    }

    @Test
    public void shouldReturnEmptyIfNoRequestMediaTypeFoundForTheActionName() throws Exception {

        final String actionName = "example.add-recipe";

        final MediaTypes mediaTypes = mock(MediaTypes.class);

        when(mediaTypesMappingCache.mediaTypesFor(actionName)).thenReturn(of(mediaTypes));
        when(mediaTypes.getRequestMediaType()).thenReturn(empty());

        assertThat(mediaTypeProvider.getRequestMediaType(actionName), is(empty()));
    }

    @Test
    public void shouldReturnEmptyIfNoResponseMediaTypeFoundForTheActionName() throws Exception {

        final String actionName = "example.add-recipe";

        final MediaTypes mediaTypes = mock(MediaTypes.class);

        when(mediaTypesMappingCache.mediaTypesFor(actionName)).thenReturn(of(mediaTypes));
        when(mediaTypes.getResponseMediaType()).thenReturn(empty());

        assertThat(mediaTypeProvider.getResponseMediaType(actionName), is(empty()));
    }
}
