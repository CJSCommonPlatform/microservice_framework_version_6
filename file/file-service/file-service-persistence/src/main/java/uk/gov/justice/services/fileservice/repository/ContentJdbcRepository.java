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

/**
 * Class for handling inserts/updates/selects on the 'content' database table. This class is not
 * transactional. Each method takes a valid database connection and it is assumed that the transaction
 * would have already been started on that connection. Any failures during insert/update will throw
 * a {@link TransactionFailedException}. In which case the current transaction should be rolled
 * back
 */
public class ContentJdbcRepository {

    private static final String SQL_FIND_BY_FILE_ID = "SELECT content FROM content WHERE file_id=? ";
    private static final String SQL_INSERT_METADATA = "INSERT INTO content(content, file_id) VALUES(?, ?)";
    private static final String SQL_UPDATE_METADATA = "UPDATE content set content = ? WHERE file_id = ?";

    private final Closer closer = new Closer();

    /**
     * Inserts the content into the content table as an array of bytes[]
     *
     * @param fileId the file id of the content
     * @param content a byte[] array of the file content
     * @param connection the database connection. It is assumed that a transaction has previously been
     *                   started on this connection.
     * @throws TransactionFailedException if the insert fails and the transaction should be rolled back.
     */
    public void insert(final UUID fileId, final byte[] content, final Connection connection) throws TransactionFailedException {
        insertOrUpdate(fileId, content, connection, SQL_INSERT_METADATA);
    }

    /**
     * Updates the content in the content table
     *
     * @param fileId the file id of the content
     * @param content a byte[] array of the file content
     * @param connection the database connection. It is assumed that a transaction has previously been
     *                   started on this connection.
     * @throws TransactionFailedException if the update fails and the transaction should be rolled back.
     */
    public void update(final UUID fileId, final byte[] content, final Connection connection) throws TransactionFailedException {
        insertOrUpdate(fileId, content, connection, SQL_UPDATE_METADATA);
    }

    /**
     * Finds the file content for the specified file id, returned as a java {@link Optional}. If no
     * content found for that id then {@code empty()} is returned instead.
     *
     * @param fileId the file id of the content
     * @param connection a live database connection
     * @return the file content as an array of bytes wrapped in a java {@link Optional}
     * @throws TransactionFailedException if the read failed and so the current transaction should
     * be rollled back.
     */
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
