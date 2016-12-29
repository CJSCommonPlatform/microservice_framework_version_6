package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.file.api.domain.StorableFile;
import uk.gov.justice.services.fileservice.datasource.DataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class TransactionalFileRepository {

    @Inject
    FileJdbcRepository fileJdbcRepository;

    @Inject
    MetadataJdbcRepository metadataJdbcRepository;

    @Inject
    DataSourceProvider dataSourceProvider;

    @Inject
    Closer closer;

    @Inject
    DatabaseConnectionUtils databaseConnectionUtils;

    public void store(final UUID fileId, final byte[] content, final JsonObject metadata) {

        final Connection connection = databaseConnectionUtils.getConnection(dataSourceProvider.getDataSource());
        final boolean autoCommit = databaseConnectionUtils.getAutoCommit(connection);

        try {
            if (autoCommit) {
                databaseConnectionUtils.setAutoCommit(false, connection);
            }

            if (fileJdbcRepository.findByFileId(fileId, connection).isPresent()) {
                fileJdbcRepository.update(fileId, content, connection);
                metadataJdbcRepository.update(fileId, metadata, connection);
            } else {
                fileJdbcRepository.insert(fileId, content, connection);
                metadataJdbcRepository.insert(fileId, metadata, connection);
            }

            databaseConnectionUtils.commit(connection);
        } catch (final DataUpdateException e) {
            databaseConnectionUtils.rollback(connection);
            throw new JdbcRepositoryException("Failed to store file with id " + fileId, e);
        } finally {
            databaseConnectionUtils.setAutoCommit(autoCommit, connection);
            closer.close(connection);
        }
    }

    public Optional<StorableFile> find(final UUID fileId) {

        final Connection connection = databaseConnectionUtils.getConnection(dataSourceProvider.getDataSource());

        try {
            final Optional<byte[]> content = fileJdbcRepository.findByFileId(fileId, connection);
            final Optional<JsonObject> metadata = metadataJdbcRepository.findByFileId(fileId, connection);

            if (! metadata.isPresent()) {
                return empty();
            }
            if (! content.isPresent()) {
                throw new JdbcRepositoryException("No file content found for file id " + fileId);
            }

            return of(new StorableFile(
                    fileId,
                    metadata.get(),
                    content.get()));

        } catch (final DataUpdateException e) {
            throw new JdbcRepositoryException("Failed to find file with id " + fileId, e);
        } finally {
            closer.close(connection);
        }
    }
}
