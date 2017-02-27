package uk.gov.justice.services.adapter.rest.interceptor;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.mutipart.FileInputDetails;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.api.FileStorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class SingleFileInputDetailsServiceTest {

    @Mock
    private FileStorer fileStorer;

    @Mock
    private Logger logger;
    
    @InjectMocks
    private SingleFileInputDetailsService singleFileInputDetailsService;

    @Test
    public void shouldStoreTheFileInputStream() throws Exception {

        final UUID fileId = randomUUID();

        final FileInputDetails fileInputDetails = mock(FileInputDetails.class);
        final InputStream inputStream = mock(InputStream.class);
        final JsonObject metadata = mock(JsonObject.class);

        when(fileInputDetails.getInputStream()).thenReturn(inputStream);
        when(fileStorer.store(metadata, inputStream)).thenReturn(fileId);

        assertThat(singleFileInputDetailsService.store(fileInputDetails, metadata), is(fileId));

        verify(inputStream).close();
    }

    @Test
    public void shouldThrowAFileStoreFailedExceptionIfStoringTheInputStreamFails() throws Exception {

        final FileServiceException fileServiceException = new FileServiceException("Ooops");

        final FileInputDetails fileInputDetails = mock(FileInputDetails.class);
        final JsonObject metadata = mock(JsonObject.class);
        final InputStream inputStream = mock(InputStream.class);

        when(fileInputDetails.getInputStream()).thenReturn(inputStream);
        when(fileStorer.store(metadata, inputStream)).thenThrow(fileServiceException);

        try {
            singleFileInputDetailsService.store(fileInputDetails, metadata);
            fail();
        } catch (final FileStoreFailedException expected) {
            assertThat(expected.getCause(), is(fileServiceException));
            assertThat(expected.getMessage(), is("Failed to store file in FileStore"));
        }

        verify(inputStream).close();
    }

    @Test
    public void shouldLogAWarningIfClosingTheInputStreamFails() throws Exception {

        final IOException ioException = new IOException("Ooops");

        final UUID fileId = randomUUID();

        final FileInputDetails fileInputDetails = mock(FileInputDetails.class);
        final JsonObject metadata = mock(JsonObject.class);
        final InputStream inputStream = mock(InputStream.class);

        when(fileInputDetails.getInputStream()).thenReturn(inputStream);
        when(fileStorer.store(metadata, inputStream)).thenReturn(fileId);
        doThrow(ioException).when(inputStream).close();

        assertThat(singleFileInputDetailsService.store(fileInputDetails, metadata), is(fileId));

        verify(logger).warn("Error closing InputStream", ioException);
    }
}
