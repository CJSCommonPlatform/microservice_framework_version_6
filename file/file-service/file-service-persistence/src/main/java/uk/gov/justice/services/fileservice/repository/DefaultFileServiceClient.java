package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.file.api.FileServiceClient;
import uk.gov.justice.services.file.api.domain.StorableFile;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class DefaultFileServiceClient implements FileServiceClient {

    @Inject
    MetadataJdbcRepository metadataJdbcRepository;

    @Inject
    FileJdbcRepository fileJdbcRepository;

    @Override
    public void store(final StorableFile storableFile) {

        final UUID fileId = storableFile.getFileId();
        final JsonObject metadata = storableFile.getMetadata();
        final byte[] content = storableFile.getContent();

        if (fileJdbcRepository.findByFileId(fileId).isPresent()) {
            update(fileId, content, metadata);
        } else {
            insert(fileId, content, metadata);
        }
    }

    @Override
    public Optional<StorableFile> find(final UUID fileId) {

        final Optional<byte[]> fileContents = fileJdbcRepository.findByFileId(fileId);
        final Optional<JsonObject> metadata = metadataJdbcRepository.findByFileId(fileId);

        if (fileContents.isPresent()) {
            if (metadata.isPresent()) {
                return of(new StorableFile(
                        fileId,
                        metadata.get(),
                        fileContents.get()));

            }

            throw new RuntimeException("Found file with id '" + fileId + "', but with no metadata in database");
        }

        return empty();
    }

    private void update(final UUID fileId, final byte[] content, final JsonObject metadata) {
        fileJdbcRepository.update(fileId, content);
        metadataJdbcRepository.update(fileId, metadata);
    }

    private void insert(final UUID fileId, final byte[] content, final JsonObject metadata) {
        fileJdbcRepository.insert(fileId, content);
        metadataJdbcRepository.insert(fileId, metadata);
    }
}
