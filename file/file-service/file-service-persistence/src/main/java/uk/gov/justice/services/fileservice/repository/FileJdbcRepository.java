package uk.gov.justice.services.fileservice.repository;

import static java.lang.String.format;
import static java.util.Optional.empty;

import uk.gov.justice.services.fileservice.datasource.DataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class FileJdbcRepository {

    private static final String SQL_FIND_BY_FILE_ID = "SELECT * FROM file WHERE file_id=? ";
    private static final String SQL_INSERT_METADATA = "INSERT INTO file(file_id,content) VALUES(?, ?)";

    @Inject
    DataSourceProvider dataSourceProvider;

    public void insert(final UUID fileId, final byte[] content) {

        try (final Connection connection = dataSourceProvider.getDataSource().getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();

            try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
                 final PreparedStatement ps = connection.prepareStatement(SQL_INSERT_METADATA)
            ) {

                if (autoCommit) {
                    connection.setAutoCommit(false);
                }

                ps.setObject(1, fileId);
                ps.setBinaryStream(2, byteArrayInputStream, content.length);
                ps.executeUpdate();

                connection.commit();
            } catch (final SQLException | IOException e) {
                connection.rollback();
                throw new JdbcRepositoryException("Exception while inserting file", e);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Exception while inserting file", e);
        }
    }

    public void update(final UUID fileId, final byte[] content) {

    }

    public Optional<byte[]> findByFileId(final UUID fileId) {

        ResultSet resultSet = null;
        try (final Connection connection = dataSourceProvider.getDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(SQL_FIND_BY_FILE_ID)) {
            ps.setObject(1, fileId);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {

                // TODO: change query
                final UUID id = (UUID) resultSet.getObject(1);
                final byte[] content = resultSet.getBytes(2);
                return Optional.of(content);
            }
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format("Exception while reading metadata %s", fileId), e);
        } finally {
            close(resultSet);
        }

        return empty();
    }

    private void close(final ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

        try {
            resultSet.close();
        } catch (final SQLException ignored) {
        }
    }
}
