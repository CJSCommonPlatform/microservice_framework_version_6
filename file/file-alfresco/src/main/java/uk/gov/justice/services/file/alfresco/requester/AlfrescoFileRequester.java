package uk.gov.justice.services.file.alfresco.requester;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.valueOf;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.file.alfresco.rest.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileData;
import uk.gov.justice.services.file.api.FileServiceUnavailableException;
import uk.gov.justice.services.file.api.requester.FileRequester;

import java.util.Collections;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

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
    public Optional<byte[]> request(final FileData fileData, final String fileName) {

        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();

        headers.put(ID, Collections.singletonList(alfrescoReadUser));

        try {
            final Response response = restClient.get(buildAlfrescoUri(fileData.getFileId(), fileName),
                    valueOf(fileData.getFileMimeType()), headers);
            final int responseStatusCode = response.getStatus();

            if (OK.getStatusCode() == responseStatusCode) {

                return ofNullable(response.readEntity(byte[].class));

            } else if (NOT_FOUND.getStatusCode() == responseStatusCode) {

                return empty();

            } else {

                throw new FileServiceUnavailableException("Alfresco is unavailable with response status code: " + responseStatusCode);
            }
        } catch (ProcessingException processingException) {

            throw new FileServiceUnavailableException("Processing exception occured while getting the resource from Alfresco " , processingException);
        }
    }

    private String buildAlfrescoUri(final String fieldId, final String fileName) {
        final StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(alfrescoWorkspacePath);
        requestBuilder.append(fieldId);
        requestBuilder.append(URL_SEPARATOR);
        requestBuilder.append("content");
        requestBuilder.append(URL_SEPARATOR);
        requestBuilder.append(fileName);
        return requestBuilder.toString();
    }
}
