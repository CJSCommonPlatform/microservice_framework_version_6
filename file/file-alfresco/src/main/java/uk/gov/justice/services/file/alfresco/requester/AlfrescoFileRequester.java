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

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

@ApplicationScoped
public class AlfrescoFileRequester implements FileRequester {
    private static final String URL_SEPARATOR = "/";

    @Inject
    @GlobalValue(key = "alfrescoWorkspacePath", defaultValue = "/service/api/node/content/workspace/SpacesStore/")
    String alfrescoWorkspacePath;

    @Inject
    @GlobalValue(key = "alfrescoReadUser")
    String alfrescoReadUser;

    @Inject
    AlfrescoRestClient restClient;

    @Override
    public Optional<byte[]> request(final String fileId, final String fileMimeType, final String fileName, final boolean stream) {

        try {
            final Response response = restClient.get(alfrescoUriOf(fileId, fileName, stream),
                    valueOf(fileMimeType), headersWithUserId(alfrescoReadUser));
            final StatusType responseStatus = response.getStatusInfo();

            if (responseStatus == OK) {
                return ofNullable(response.readEntity(byte[].class));
            } else if (responseStatus == NOT_FOUND) {
                return empty();
            } else {
                throw new FileOperationException(format("Alfresco is unavailable with response status code: %d", responseStatus.getStatusCode()));
            }
        } catch (ProcessingException processingException) {
            throw new FileOperationException("Error fetching resource from Alfresco", processingException);
        }
    }

    private String alfrescoUriOf(final String fieldId, final String fileName, final boolean stream) {
        final StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(alfrescoWorkspacePath);
        requestBuilder.append(fieldId);
        requestBuilder.append(URL_SEPARATOR);
        requestBuilder.append("content");
        requestBuilder.append(URL_SEPARATOR);
        requestBuilder.append(fileName);
        // a means attach -> a = true means don't stream!
        if (!stream) {
            requestBuilder.append("?a=true");
        }
        return requestBuilder.toString();
    }
}
