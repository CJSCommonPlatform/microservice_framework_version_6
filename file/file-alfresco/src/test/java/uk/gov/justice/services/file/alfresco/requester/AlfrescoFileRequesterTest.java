package uk.gov.justice.services.file.alfresco.requester;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.file.alfresco.common.Headers.headersWithUserId;

import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoFileRequesterTest {

    private static final String FILE_MIME_TYPE = TEXT_PLAIN;
    private static final String FILE_NAME = "abc.txt";

    @Mock
    AlfrescoRestClient alfrescoRestClient;

    @Mock
    Response response;

    @Mock
    InputStream inputStream;

    @InjectMocks
    AlfrescoFileRequester alfrescoFileRequester;


    @Test
    public void shouldRequestStreamedFileAndReceiveInputStreamWhenFileIsFound() throws IOException {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();
        final String fileId = randomUUID().toString();

        when(alfrescoRestClient.getAsInputStream(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, FILE_NAME),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenReturn(inputStream);

        final Optional<InputStream> actualInputStream =
                alfrescoFileRequester.request(fileId, FILE_MIME_TYPE, FILE_NAME);

        assertTrue(actualInputStream.isPresent());
        assertEquals(inputStream, actualInputStream.get());

    }

    @Test
    public void shouldRequestFileAsStreamAndReceiveEmptyWhenFileIsNotFound() {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();
        final String fileId = randomUUID().toString();
        final NotFoundException notFoundException = new NotFoundException("oops");

        when(alfrescoRestClient.getAsInputStream(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, FILE_NAME),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenThrow(notFoundException);

        final Optional<InputStream> actualInputStream =
                alfrescoFileRequester.request(fileId, FILE_MIME_TYPE, FILE_NAME);

        assertFalse(actualInputStream.isPresent());
    }

    @Test
    public void shouldThrowFileOperationExceptionIfProcessingExceptionOccursWhenConnectingToAlfrescoWhenRequestingFileAsStream() {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();
        final String fileId = randomUUID().toString();
        final ProcessingException processingException = new ProcessingException("oops");

        when(alfrescoRestClient.getAsInputStream(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, FILE_NAME),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenThrow(processingException);

        try {
            alfrescoFileRequester.request(fileId, FILE_MIME_TYPE, FILE_NAME);
            fail("Was expecting a FileOperationException to be thrown");
        } catch (final FileOperationException foe) {
            assertEquals(processingException, foe.getCause());
            assertEquals(format("Error fetching %s from Alfresco with fileId = %s", FILE_NAME, fileId),
                    foe.getMessage());
        }
    }

    @Test
    public void shouldThrowFileOperationExceptionIfInternalServerErrorExceptionOccursWhenConnectingToAlfrescoWhenRequestingFileAsStream() {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();
        final String fileId = randomUUID().toString();
        final InternalServerErrorException internalServerErrorException = new InternalServerErrorException("oops");

        when(alfrescoRestClient.getAsInputStream(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, FILE_NAME),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenThrow(internalServerErrorException);

        try {
            alfrescoFileRequester.request(fileId, FILE_MIME_TYPE, FILE_NAME);
            fail("Was expecting a FileOperationException to be thrown");
        } catch (final FileOperationException foe) {
            assertEquals(internalServerErrorException, foe.getCause());
            assertEquals(format("Error fetching %s from Alfresco with fileId = %s", FILE_NAME, fileId),
                    foe.getMessage());
        }
    }

}
