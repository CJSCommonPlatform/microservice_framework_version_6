package uk.gov.justice.services.fileservice.client;

import uk.gov.justice.services.file.api.FileServiceClient;
import uk.gov.justice.services.file.api.domain.StorableFile;
import uk.gov.justice.services.fileservice.repository.TransactionalFileStore;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class DefaultFileServiceClient implements FileServiceClient {

    @Inject
    TransactionalFileStore transactionalFileStore;

    @Override
    public void store(final StorableFile storableFile) {

        final UUID fileId = storableFile.getFileId();
        final JsonObject metadata = storableFile.getMetadata();
        final byte[] content = storableFile.getContent();

        transactionalFileStore.store(fileId, content, metadata);
    }

    @Override
    public Optional<StorableFile> find(final UUID fileId) {
        return transactionalFileStore.find(fileId);
    }
}
