package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.file.api.domain.StorableFile;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;


/**
 * Stores/finds file content and metadata in the database.
 *
 * NB. This class does not consider transactions as each method considers itself to already
 * be running inside a transaction. Therefore, it is the responsibility of the calling class
 * to manage transactions on its connections.
 */
public class FileStore {

    @Inject
    ContentJdbcRepository contentJdbcRepository;

    @Inject
    MetadataJdbcRepository metadataJdbcRepository;

    /**
     * Stores file content and metadata in the database
     *
     * @param fileId the file id
     * @param content the file content in bytes[]
     * @param metadata the file metadata json
     * @param connection the database connection. Assumes a transaction has been started on this
     *                   Connection
     * @throws TransactionFailedException if any of the database updates failed and the transaction
     * should be rolled back
     */
    public void store(
            final UUID fileId,
            final byte[] content,
            final JsonObject metadata,
            final Connection connection) throws TransactionFailedException {

        if (contentJdbcRepository.findByFileId(fileId, connection).isPresent()) {
            contentJdbcRepository.update(fileId, content, connection);
            metadataJdbcRepository.update(fileId, metadata, connection);
        } else {
            contentJdbcRepository.insert(fileId, content, connection);
            metadataJdbcRepository.insert(fileId, metadata, connection);
        }
    }

    /**
     * Finds file content and metadata for a specified file id. If no content/metadata exists
     * for that id then {@link Optional}.empty() is returned.
     *
     * If no metadata is found for that id then {@code empty()} is returned
     * If metadata is found but no content, then an {@link TransactionFailedException} is thrown
     *
     * @param fileId the id of the file
     * @param connection a database connection
     * @return if found: the {@link StorableFile} for that id wrapped in an {@link Optional}
     * @return if not found: {@link Optional}.empty()
     * @throws TransactionFailedException if any of the database updates failed and the transaction
     * should be rolled back
     */
    public Optional<StorableFile> find(final UUID fileId, final Connection connection) throws TransactionFailedException {

        final Optional<JsonObject> metadata = metadataJdbcRepository.findByFileId(fileId, connection);
        final Optional<byte[]> content = contentJdbcRepository.findByFileId(fileId, connection);

        if (! metadata.isPresent()) {
            return empty();
        }
        if (! content.isPresent()) {
            throw new TransactionFailedException("No file content found for file id " + fileId + " but metadata exists for that id");
        }

        return of(new StorableFile(
                fileId,
                metadata.get(),
                content.get()));
    }
}
