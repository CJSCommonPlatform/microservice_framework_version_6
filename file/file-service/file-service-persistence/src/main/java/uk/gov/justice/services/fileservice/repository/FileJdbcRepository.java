package uk.gov.justice.services.fileservice.repository;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class FileJdbcRepository {

    private static final String SQL_FIND_BY_FILE_ID = "SELECT content FROM file WHERE file_id=? ";
    private static final String SQL_INSERT_METADATA = "INSERT INTO file(content, file_id) VALUES(?, ?)";
    private static final String SQL_UPDATE_METADATA = "UPDATE file set content = ? WHERE file_id = ?";

    private final Closer closer = new Closer();

    public void insert(final UUID fileId, final byte[] content, final Connection connection) throws TransactionFailedException {
        insertOrUpdate(fileId, content, connection, SQL_INSERT_METADATA);
    }

    public void update(final UUID fileId, final byte[] content, final Connection connection) throws TransactionFailedException {
        insertOrUpdate(fileId, content, connection, SQL_UPDATE_METADATA);
    }

    public Optional<byte[]> findByFileId(final UUID fileId, final Connection connection) throws TransactionFailedException {

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(SQL_FIND_BY_FILE_ID);
            preparedStatement.setObject(1, fileId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return of(resultSet.getBytes(1));
            }
        } catch (final SQLException e) {
            throw new TransactionFailedException(format("Exception while reading metadata %s", fileId), e);
        } finally {
            closer.close(resultSet, preparedStatement);
        }

        return empty();
    }

    private void insertOrUpdate(
            final UUID fileId,
            final byte[] content,
            final Connection connection,
            final String sql) throws TransactionFailedException {

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setBinaryStream(1, byteArrayInputStream, content.length);
            preparedStatement.setObject(2, fileId);
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new TransactionFailedException("Exception while inserting file", e);
        } finally {
            closer.close(byteArrayInputStream, preparedStatement);
        }
    }
}
