package uk.gov.justice.services.fileservice.client;

import uk.gov.justice.services.file.api.domain.StorableFile;
import uk.gov.justice.services.fileservice.repository.TransactionalFileStore;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

/**
 * Implementation of the {@link FileServiceClient} interface, as the entry point into
 * the file service.
 *
 * Currently this class is a simple wrapper around {@link TransactionalFileStore}, kept
 * separate simply for isolation of responsibilities
 */
public class DefaultFileServiceClient implements FileServiceClient {

    @Inject
    TransactionalFileStore transactionalFileStore;

    /**
     * Stores a file in the database. This method is fully idempotent as the code checkes
     * whether this file exists in the database before deciding whether to insert or update.
     *
     * @param storableFile The file to be stored.
     */
    @Override
    public void store(final StorableFile storableFile) {

        final UUID fileId = storableFile.getFileId();
        final JsonObject metadata = storableFile.getMetadata();
        final byte[] content = storableFile.getContent();

        transactionalFileStore.store(fileId, content, metadata);
    }

    /**
     * Finds the {@link StorableFile} for the specified id, wrapped in an {@link Optional}
     * If no file found then {@code empty()} is returned.
     *
     * @param fileId the id of the required file
     * @return the {@link StorableFile} or {@code empty()} if not found
     */
    @Override
    public Optional<StorableFile> find(final UUID fileId) {
        return transactionalFileStore.find(fileId);
    }
}
