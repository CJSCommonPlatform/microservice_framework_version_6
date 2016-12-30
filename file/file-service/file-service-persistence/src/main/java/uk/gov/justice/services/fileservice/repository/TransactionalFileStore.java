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

public class TransactionalFileStore {

    @Inject
    FileStore fileStore;

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

            fileStore.store(fileId, content, metadata, connection);

            databaseConnectionUtils.commit(connection);
        } catch (final DataUpdateException e) {
            databaseConnectionUtils.rollback(connection);
            throw new JdbcRepositoryException("Failed to store file with id " + fileId, e);
        } finally {
            try {
                databaseConnectionUtils.setAutoCommit(autoCommit, connection);
            } finally {
                closer.close(connection);
            }
        }
    }

    public Optional<StorableFile> find(final UUID fileId) {

        final Connection connection = databaseConnectionUtils.getConnection(dataSourceProvider.getDataSource());

        try {
            return fileStore.find(fileId, connection);

        } catch (final DataUpdateException e) {
            throw new JdbcRepositoryException("Failed to find file with id " + fileId, e);
        } finally {
            closer.close(connection);
        }
    }
}
