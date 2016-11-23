package uk.gov.justice.services.file.alfresco.requester;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.valueOf;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.services.file.alfresco.common.Headers.headersWithUserId;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;
import uk.gov.justice.services.file.api.requester.FileRequester;
import uk.gov.justice.services.file.api.requester.StreamingFileRequester;

import java.io.InputStream;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

@ApplicationScoped
public class AlfrescoFileRequester implements StreamingFileRequester, FileRequester {

    private static final String DO_NOT_STREAM = "?a=true";

    @Inject
    @GlobalValue(key = "alfrescoWorkspacePath", defaultValue = "/service/api/node/content/workspace/SpacesStore/")
    String alfrescoWorkspacePath;

    @Inject
    @GlobalValue(key = "alfrescoReadUser")
    String alfrescoReadUser;

    @Inject
    AlfrescoRestClient restClient;

    @Override
    public Optional<byte[]> request(final String fileId, final String fileMimeType, final String fileName) {
        try {
            final Response response = restClient.get(alfrescoUriOf(fileId, fileName),
                    valueOf(fileMimeType), headersWithUserId(alfrescoReadUser));
            final StatusType responseStatus = response.getStatusInfo();

            if (responseStatus == OK) {
                return ofNullable(response.readEntity(byte[].class));
            } else if (responseStatus == NOT_FOUND) {
                return empty();
            }
            throw new FileOperationException(format("Alfresco is unavailable with response status code: %d",
                    responseStatus.getStatusCode()));

        } catch (final ProcessingException processingException) {
            throw new FileOperationException(format("Error fetching %s from Alfresco with fileId = %s",
                    fileName, fileId), processingException);
        }
    }

    @Override
    public Optional<InputStream> requestStreamed(final String fileId, final String fileMimeType, final String fileName) {
        try {
            return ofNullable(restClient.getAsInputStream(alfrescoStreamUriOf(fileId, fileName),
                    valueOf(fileMimeType), headersWithUserId(alfrescoReadUser)));
        } catch (final NotFoundException nfe) {
            return empty();
        } catch (final ProcessingException | InternalServerErrorException ex ) {
            throw new FileOperationException(format("Error fetching %s from Alfresco with fileId = %s",
                    fileName, fileId), ex);
        }
    }

    private String alfrescoUriOf(final String fileId, final String fileName) {
        return format("%s%s", alfrescoStreamUriOf(fileId, fileName), DO_NOT_STREAM);
    }

    private String alfrescoStreamUriOf(final String fileId, final String fileName) {
        return format("%s%s/content/%s", alfrescoWorkspacePath, fileId, fileName);
    }

}
