package uk.gov.justice.services.file.alfresco.requester;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.valueOf;
import static uk.gov.justice.services.file.alfresco.common.Headers.headersWithUserId;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;
import uk.gov.justice.services.file.api.requester.FileRequester;

import java.io.InputStream;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;

@ApplicationScoped
public class AlfrescoFileRequester implements FileRequester {


    @Inject
    @GlobalValue(key = "alfrescoWorkspacePath", defaultValue = "/service/api/node/content/workspace/SpacesStore/")
    String alfrescoWorkspacePath;

    @Inject
    @GlobalValue(key = "alfrescoPdfContentWorkspacePath", defaultValue = "/service/api/node/workspace/SpacesStore/")
    String alfrescoPdfContentWorkspacePath;

    @Inject
    @GlobalValue(key = "alfrescoReadUser")
    String alfrescoReadUser;

    @Inject
    AlfrescoRestClient restClient;

    @Override
    public Optional<InputStream> request(final String fileId, final String fileMimeType, final String fileName) {
        try {
            return ofNullable(restClient.getAsInputStream(alfrescoUriOf(fileId, fileName, false),
                    valueOf(fileMimeType), headersWithUserId(alfrescoReadUser)));
        } catch (final NotFoundException nfe) {
            return empty();
        } catch (final ProcessingException | InternalServerErrorException ex) {
            throw new FileOperationException(format("Error fetching %s from Alfresco with fileId = %s",
                    fileName, fileId), ex);
        }
    }

    @Override
    public Optional<InputStream> request(final String fileId, final String fileMimeType, final String fileName, final boolean transformPdf) {
        try {
            return ofNullable(restClient.getAsInputStream(alfrescoUriOf(fileId, fileName, transformPdf),
                    valueOf(fileMimeType), headersWithUserId(alfrescoReadUser)));
        } catch (final NotFoundException nfe) {
            return empty();
        } catch (final ProcessingException | InternalServerErrorException ex) {
            throw new FileOperationException(format("Error fetching %s from Alfresco with fileId = %s",
                    fileName, fileId), ex);
        }
    }

    private String alfrescoUriOf(final String fileId, final String fileName, final boolean transformPdf) {
        if(transformPdf) {
            return format("%s%s?transformpdf=%s", alfrescoPdfContentWorkspacePath, fileId, transformPdf);
        }
        return format("%s%s/content/%s", alfrescoWorkspacePath, fileId, fileName);
    }

}
