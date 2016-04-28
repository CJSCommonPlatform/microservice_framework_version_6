package uk.gov.justice.services.adapter.rest.envelope;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

/**
 * Unit tests for the {@link StructuredMediaType} class.
 */
public class StructuredMediaTypeTest {

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfNoNameFound() throws Exception {
        StructuredMediaType mediaType = new StructuredMediaType(MediaType.APPLICATION_JSON_TYPE);
        mediaType.getName();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfMoreThanOneNameFound() throws Exception {
        StructuredMediaType mediaType = new StructuredMediaType(new MediaType("application", "json+vnd.blah+vnd.blah"));
        mediaType.getName();
    }

    @Test
    public void shouldReturnName() throws Exception {
        StructuredMediaType mediaType = new StructuredMediaType(new MediaType("application", "vnd.blah+json"));
        String name = mediaType.getName();
        assertThat(name, equalTo("blah"));
    }
}
