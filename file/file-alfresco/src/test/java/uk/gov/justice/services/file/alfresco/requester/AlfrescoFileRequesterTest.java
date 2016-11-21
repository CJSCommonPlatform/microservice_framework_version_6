package uk.gov.justice.services.file.alfresco.requester;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.file.alfresco.common.Headers.headersWithUserId;

import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;

import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoFileRequesterTest {

    @Mock
    AlfrescoRestClient alfrescoRestClient;

    @Mock
    Response response;

    @InjectMocks
    AlfrescoFileRequester alfrescoFileRequester;

    @Test
    public void shouldRequestAFileAndReceiveByteArrayWhenFileIsFound() {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();

        final String fileId = randomUUID().toString();
        final String fileMimeType = MediaType.TEXT_PLAIN;
        final String fileName = "fileName";
        final boolean stream = true;
        final String expectedFileOutput = "This is a test file";

        when(alfrescoRestClient.get(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, fileName),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenReturn(response);

        when(response.readEntity(byte[].class)).thenReturn(expectedFileOutput.getBytes());
        when(response.getStatusInfo()).thenReturn(OK);

        final Optional<byte[]> actualData =
                alfrescoFileRequester.request(fileId, fileMimeType, fileName, stream);

        assertTrue(actualData.isPresent());
        assertArrayEquals(expectedFileOutput.getBytes(), actualData.get());
    }

    @Test
    public void shouldRequestAFileAndReceiveEmptyResponseWhenFileIsNotFound() {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();

        final String fileId = randomUUID().toString();
        final String fileMimeType = MediaType.TEXT_PLAIN;
        final String fileName = "fileName";
        final boolean stream = true;

        when(alfrescoRestClient.get(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, fileName),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenReturn(response);

        when(response.getStatusInfo()).thenReturn(NOT_FOUND);

        final Optional<byte[]> actualData =
                alfrescoFileRequester.request(fileId, fileMimeType, fileName, stream);

        assertFalse(actualData.isPresent());
    }

    @Test(expected = FileOperationException.class)
    public void shoulThrowFileOperationExceptionIfResponseStatusCodeIsNotOkAndNotFound() {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();

        final String fileId = randomUUID().toString();
        final String fileMimeType = MediaType.TEXT_PLAIN;
        final String fileName = "fileName";
        final boolean stream = true;

        when(alfrescoRestClient.get(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, fileName),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenReturn(response);

        when(response.getStatusInfo()).thenReturn(BAD_REQUEST);

        alfrescoFileRequester.request(fileId, fileMimeType, fileName, stream);
    }

    @Test(expected = FileOperationException.class)
    public void shoulThrowFileOperationExceptionIfProcessingExceptionOccursWhenConnectingToAlfresco() {
        alfrescoFileRequester.alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        alfrescoFileRequester.alfrescoReadUser = randomUUID().toString();

        final String fileId = randomUUID().toString();
        final String fileMimeType = MediaType.TEXT_PLAIN;
        final String fileName = "fileName";
        final boolean stream = true;

        when(alfrescoRestClient.get(format("%s%s/content/%s",
                alfrescoFileRequester.alfrescoWorkspacePath, fileId, fileName),
                TEXT_PLAIN_TYPE, headersWithUserId(alfrescoFileRequester.alfrescoReadUser)))
                .thenThrow(ProcessingException.class);

        alfrescoFileRequester.request(fileId, fileMimeType, fileName, stream);
    }
}
