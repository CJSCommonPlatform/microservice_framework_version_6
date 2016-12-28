package uk.gov.justice.services.fileservice.repository;

import static java.lang.String.format;
import static java.util.Optional.empty;

import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
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
import javax.naming.NamingException;
import javax.sql.DataSource;

public class FileJdbcRepository extends AbstractJdbcRepository<File> {

    private static final String SQL_FIND_BY_FILE_ID = "SELECT * FROM file WHERE file_id=? ";
    private static final String SQL_INSERT_METADATA = "INSERT INTO file(file_id,content) VALUES(?, ?)";
    private static final String JNDI_DS_FILE_STORE_PATTERN = "java:/app/file-service-persistence/DS.filestore";

    @Inject
    DataSourceProvider dataSourceProvider;

    @Override
    protected DataSource getDataSource() {
        return dataSourceProvider.getDataSource();
    }

    public void insert(final File file) {

        final byte[] content = file.getContent();

        try (final Connection connection = getDataSource().getConnection();
             final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
             final PreparedStatement ps = connection.prepareStatement(SQL_INSERT_METADATA)
        ) {

            ps.setObject(1, file.getFileId());
            ps.setBinaryStream(2, byteArrayInputStream, content.length);
            ps.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new JdbcRepositoryException(format("Exception while storing metadata %s", file.getFileId()), e);
        }
    }

    public Optional<File> findByFileId(final UUID fileId) {

        ResultSet resultSet = null;
        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(SQL_FIND_BY_FILE_ID)) {
            ps.setObject(1, fileId);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                final UUID id = (UUID) resultSet.getObject(1);
                final byte[] content = resultSet.getBytes(2);
                return Optional.of(new File(id, content));
            }
        } catch (SQLException e) {
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
        } catch (SQLException ignored) {
        }
    }

    @Override
    protected String jndiName() throws NamingException {
        return JNDI_DS_FILE_STORE_PATTERN;
    }

    @Override
    protected File entityFrom(final ResultSet resultSet) throws SQLException {

        final UUID id = (UUID) resultSet.getObject(1);
        final byte[] content = resultSet.getBytes(2);

        return new File(id, content);
    }
}
