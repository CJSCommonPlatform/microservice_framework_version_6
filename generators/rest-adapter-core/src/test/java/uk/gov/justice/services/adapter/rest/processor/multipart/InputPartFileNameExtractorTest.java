package uk.gov.justice.services.adapter.rest.processor.multipart;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.adapter.rest.mutipart.InputPartFileNameExtractor;

import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InputPartFileNameExtractorTest {

    @InjectMocks
    private InputPartFileNameExtractor inputPartFileNameExtractor;

    @Test
    public void shouldExtractTheFileNameFromTheContentDispositionHeader() throws Exception {

        final String headerName = "Content-Disposition";
        final String headerValue = "form-data; name=\"file\"; filename=\"your_file.zip\"";
        final Map<String, String> headers = of(headerName, headerValue);

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>(headers));

        assertThat(inputPartFileNameExtractor.extractFileName(inputPart), is("your_file.zip"));
    }

    @Test
    public void shouldThrowABadRequestExceptionIfNoContentDispositionHeaderFound() throws Exception {

        final String headerName = "Some-Other-Header-Name";
        final String headerValue = "form-data; name=\"file\"; filename=\"your_file.zip\"";
        final Map<String, String> headers = of(headerName, headerValue);

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>(headers));

        try {
            inputPartFileNameExtractor.extractFileName(inputPart);
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("No header found named 'Content-Disposition'"));
        }
    }

    @Test
    public void shouldThrowABadRequestExceptionIfNoHeadersFound() throws Exception {

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>());

        try {
            inputPartFileNameExtractor.extractFileName(inputPart);
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("No header found named 'Content-Disposition'"));
        }
    }

    @Test
    public void shouldThrowABadRequestExceptionIfNoFilenameFoundInContentDispositionHeader() throws Exception {

        final String headerName = "Content-Disposition";
        final String headerValue = "form-data; name=\"file\"";
        final Map<String, String> headers = of(headerName, headerValue);

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>(headers));

        try {
            inputPartFileNameExtractor.extractFileName(inputPart);
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("Failed to find 'filename' in 'Content-Disposition' header"));
        }
    }
}