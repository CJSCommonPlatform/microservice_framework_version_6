package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.file.api.domain.StorableFile;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class FileStore {

    @Inject
    FileJdbcRepository fileJdbcRepository;

    @Inject
    MetadataJdbcRepository metadataJdbcRepository;

    public void store(final UUID fileId, final byte[] content, final JsonObject metadata, final Connection connection) throws DataUpdateException {

        if (fileJdbcRepository.findByFileId(fileId, connection).isPresent()) {
            fileJdbcRepository.update(fileId, content, connection);
            metadataJdbcRepository.update(fileId, metadata, connection);
        } else {
            fileJdbcRepository.insert(fileId, content, connection);
            metadataJdbcRepository.insert(fileId, metadata, connection);
        }
    }

    public Optional<StorableFile> find(final UUID fileId, final Connection connection) throws DataUpdateException {

        final Optional<JsonObject> metadata = metadataJdbcRepository.findByFileId(fileId, connection);
        final Optional<byte[]> content = fileJdbcRepository.findByFileId(fileId, connection);

        if (! metadata.isPresent()) {
            return empty();
        }
        if (! content.isPresent()) {
            throw new DataUpdateException("No file content found for file id " + fileId + " but metadata exists for that id");
        }

        return of(new StorableFile(
                fileId,
                metadata.get(),
                content.get()));
    }
}
