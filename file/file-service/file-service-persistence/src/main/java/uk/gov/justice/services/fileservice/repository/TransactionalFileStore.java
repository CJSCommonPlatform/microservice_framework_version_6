package uk.gov.justice.services.fileservice.repository;

import uk.gov.justice.services.file.api.domain.StorableFile;
import uk.gov.justice.services.fileservice.datasource.DataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

/**
 * Handles just the transactionality of storing/finding a file, but not any of the actual
 * reads/inserts/updates. This is to separate the transactionality from any of the lower level
 * database access.
 *
 * NB: You may notice that no transaction isolation level is set. This is because it is assumed
 * that the database is Postgres, who's default isolation level is
 * {@link Connection}.TRANSACTION_READ_COMMITTED}
 */
public class TransactionalFileStore {

    @Inject
    FileStore fileStore;

    @Inject
    DataSourceProvider dataSourceProvider;

    @Inject
    Closer closer;

    @Inject
    DatabaseConnectionUtils databaseConnectionUtils;

    /**
     * Handles the transactions for storign a file content/metadata into the database.
     * Will commit inserts/updates if successful or roll back if not.
     *
     * This method is fully idempotent, as the database is checked before insert to see if that
     * id already exists. If it does exist then an update is performed instead.
     *
     * @param fileId the id of the file
     * @param content the content if the file in bytes[]
     * @param metadata the json metadata of the file
     */
    public void store(final UUID fileId, final byte[] content, final JsonObject metadata) {

        final Connection connection = databaseConnectionUtils.getConnection(dataSourceProvider.getDataSource());
        final boolean autoCommit = databaseConnectionUtils.getAutoCommit(connection);

        try {
            if (autoCommit) {
                databaseConnectionUtils.setAutoCommit(false, connection);
            }

            fileStore.store(fileId, content, metadata, connection);

            databaseConnectionUtils.commit(connection);
        } catch (final TransactionFailedException e) {
            databaseConnectionUtils.rollback(connection);
            throw new JdbcRepositoryException("Failed to store file with id " + fileId, e);
        } finally {
            try {
                if(autoCommit) {
                    databaseConnectionUtils.setAutoCommit(true, connection);
                }
            } finally {
                closer.close(connection);
            }
        }
    }

    /**
     * finds file content/metadata for the specifed file id
     *
     * @param fileId of the file
     * @return if found the {@link StorableFile} with that id
     * @return if not found {@code Optional.empty()}
     *
     * NB as this is a read then no transactions are started. By default Postgress will only
     * read committed data
     */
    public Optional<StorableFile> find(final UUID fileId) {

        final Connection connection = databaseConnectionUtils.getConnection(dataSourceProvider.getDataSource());

        try {
            return fileStore.find(fileId, connection);
        } catch (final TransactionFailedException e) {
            throw new JdbcRepositoryException("Failed to find file with id " + fileId, e);
        } finally {
            closer.close(connection);
        }
    }
}
