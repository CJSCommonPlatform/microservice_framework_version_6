package uk.gov.justice.services.adapter.rest.interceptor;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.api.FileStorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;

@ApplicationScoped
public class SingleFileInputDetailsService {

    @Inject
    FileStorer fileStorer;

    @Inject
    Logger logger;

    public UUID store(final FileInputDetails fileInputDetails, final JsonObject metadata) {

        final InputStream inputStream = fileInputDetails.getInputStream();
        try {
            return fileStorer.store(metadata, inputStream);
        } catch (final FileServiceException e) {
            throw new FileStoreFailedException("Failed to store file in FileStore", e);
        } finally {
            close(inputStream);
        }
    }

    private void close(final InputStream inputStream) {

        try {
            inputStream.close();
        } catch (final IOException e) {
            logger.warn("Error closing InputStream", e);
        }
    }

}
