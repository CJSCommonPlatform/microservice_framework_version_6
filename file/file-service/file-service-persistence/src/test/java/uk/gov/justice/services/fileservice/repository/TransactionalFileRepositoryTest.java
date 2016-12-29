package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.fileservice.datasource.DataSourceProvider;

import java.sql.Connection;
import java.util.UUID;

import javax.json.JsonObject;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TransactionalFileRepositoryTest {

    @Mock
    private FileJdbcRepository fileJdbcRepository;

    @Mock
    private MetadataJdbcRepository metadataJdbcRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSourceProvider dataSourceProvider;

    @Mock
    private DatabaseConnectionUtils databaseConnectionUtils;

    @Mock
    private Closer closer;

    @InjectMocks
    private TransactionalFileRepository transactionalFileRepository;

    @Test
    public void shouldInsertAFileIntoTheDatabaseIfItHasNotBeenStoredPreviously() throws Exception {

        final UUID fileId = UUID.randomUUID();
        final byte[] content = "the file content".getBytes();
        final JsonObject metadata = mock(JsonObject.class);

        final boolean autoCommit = false;

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);


        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(databaseConnectionUtils.getAutoCommit(connection)).thenReturn(autoCommit);
        when(fileJdbcRepository.findByFileId(fileId, connection)).thenReturn(empty());

        transactionalFileRepository.store(fileId, content, metadata);

        final InOrder inOrder = inOrder(
                fileJdbcRepository,
                metadataJdbcRepository,
                databaseConnectionUtils,
                closer);

        inOrder.verify(fileJdbcRepository).insert(fileId, content, connection);
        inOrder.verify(metadataJdbcRepository).insert(fileId, metadata, connection);
        inOrder.verify(databaseConnectionUtils).commit(connection);
        inOrder.verify(databaseConnectionUtils).setAutoCommit(autoCommit, connection);
        inOrder.verify(closer).close(connection);
    }

    @Test
    public void shouldUpdateAFileIntoTheDatabaseIfItHasBeenStoredPreviously() throws Exception {

        final UUID fileId = UUID.randomUUID();
        final byte[] content = "the file content".getBytes();
        final JsonObject metadata = mock(JsonObject.class);

        final boolean autoCommit = false;

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(databaseConnectionUtils.getAutoCommit(connection)).thenReturn(autoCommit);
        when(fileJdbcRepository.findByFileId(fileId, connection)).thenReturn(of(content));

        transactionalFileRepository.store(fileId, content, metadata);

        final InOrder inOrder = inOrder(
                fileJdbcRepository,
                metadataJdbcRepository,
                databaseConnectionUtils,
                closer);

        inOrder.verify(fileJdbcRepository).update(fileId, content, connection);
        inOrder.verify(metadataJdbcRepository).update(fileId, metadata, connection);
        inOrder.verify(databaseConnectionUtils).commit(connection);
        inOrder.verify(databaseConnectionUtils).setAutoCommit(autoCommit, connection);
        inOrder.verify(closer).close(connection);
    }

    @Test
    public void shouldSetAutoCommitToFalseWhenInserting() throws Exception {

        final UUID fileId = UUID.randomUUID();
        final byte[] content = "the file content".getBytes();
        final JsonObject metadata = mock(JsonObject.class);

        final boolean autoCommit = true;

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);


        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(databaseConnectionUtils.getAutoCommit(connection)).thenReturn(autoCommit);
        when(fileJdbcRepository.findByFileId(fileId, connection)).thenReturn(empty());

        transactionalFileRepository.store(fileId, content, metadata);

        final InOrder inOrder = inOrder(
                databaseConnectionUtils,
                fileJdbcRepository,
                metadataJdbcRepository,
                databaseConnectionUtils,
                closer);

        inOrder.verify(databaseConnectionUtils).setAutoCommit(false, connection);
        inOrder.verify(fileJdbcRepository).insert(fileId, content, connection);
        inOrder.verify(metadataJdbcRepository).insert(fileId, metadata, connection);
        inOrder.verify(databaseConnectionUtils).commit(connection);
        inOrder.verify(databaseConnectionUtils).setAutoCommit(autoCommit, connection);
        inOrder.verify(closer).close(connection);
    }

    @Test
    public void shouldSetAutoCommitToFalseWhenUpdating() throws Exception {

        final UUID fileId = UUID.randomUUID();
        final byte[] content = "the file content".getBytes();
        final JsonObject metadata = mock(JsonObject.class);

        final boolean autoCommit = true;

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(databaseConnectionUtils.getAutoCommit(connection)).thenReturn(autoCommit);
        when(fileJdbcRepository.findByFileId(fileId, connection)).thenReturn(of(content));

        transactionalFileRepository.store(fileId, content, metadata);

        final InOrder inOrder = inOrder(
                databaseConnectionUtils,
                fileJdbcRepository,
                metadataJdbcRepository,
                databaseConnectionUtils,
                closer);

        inOrder.verify(databaseConnectionUtils).setAutoCommit(false, connection);
        inOrder.verify(fileJdbcRepository).update(fileId, content, connection);
        inOrder.verify(metadataJdbcRepository).update(fileId, metadata, connection);
        inOrder.verify(databaseConnectionUtils).commit(connection);
        inOrder.verify(databaseConnectionUtils).setAutoCommit(autoCommit, connection);
        inOrder.verify(closer).close(connection);
    }
}
