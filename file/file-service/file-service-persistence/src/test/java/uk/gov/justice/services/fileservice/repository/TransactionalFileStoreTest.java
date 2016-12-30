package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.file.api.domain.StorableFile;
import uk.gov.justice.services.fileservice.datasource.DataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.util.Optional;
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
public class TransactionalFileStoreTest {


    @Mock
    private FileStore fileStore;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSourceProvider dataSourceProvider;

    @Mock
    private DatabaseConnectionUtils databaseConnectionUtils;

    @Mock
    private Closer closer;

    @InjectMocks
    private TransactionalFileStore transactionalFileStore;

    @Test
    public void shouldStoreAFileInTheDatabase() throws Exception {

        final UUID fileId = randomUUID();
        final byte[] content = "the file content".getBytes();
        final JsonObject metadata = mock(JsonObject.class);

        final boolean autoCommit = false;

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(databaseConnectionUtils.getAutoCommit(connection)).thenReturn(autoCommit);

        transactionalFileStore.store(fileId, content, metadata);

        final InOrder inOrder = inOrder(
                fileStore,
                databaseConnectionUtils,
                closer);

        inOrder.verify(fileStore).store(fileId, content, metadata, connection);
        inOrder.verify(databaseConnectionUtils).commit(connection);
        inOrder.verify(databaseConnectionUtils).setAutoCommit(autoCommit, connection);
        inOrder.verify(closer).close(connection);
    }

    @Test
    public void shouldSetAutoCommitToFalseWhenStoring() throws Exception {

        final UUID fileId = randomUUID();
        final byte[] content = "the file content".getBytes();
        final JsonObject metadata = mock(JsonObject.class);

        final boolean autoCommit = true;

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);


        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(databaseConnectionUtils.getAutoCommit(connection)).thenReturn(autoCommit);
        when(fileStore.find(fileId, connection)).thenReturn(empty());

        transactionalFileStore.store(fileId, content, metadata);

        final InOrder inOrder = inOrder(
                databaseConnectionUtils,
                fileStore,
                databaseConnectionUtils,
                closer);

        inOrder.verify(databaseConnectionUtils).setAutoCommit(false, connection);
        inOrder.verify(fileStore).store(fileId, content, metadata, connection);
        inOrder.verify(databaseConnectionUtils).commit(connection);
        inOrder.verify(databaseConnectionUtils).setAutoCommit(true, connection);
        inOrder.verify(closer).close(connection);
    }

    @Test
    public void shouldAlwaysRollbackIfTheFileStoreThrowsAnExceptionWhenStoring() throws Exception {

        final UUID fileId = randomUUID();
        final byte[] content = "the file content".getBytes();
        final JsonObject metadata = mock(JsonObject.class);

        final Throwable dataUpdateException = new DataUpdateException("Ooops");

        final boolean autoCommit = false;

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(databaseConnectionUtils.getAutoCommit(connection)).thenReturn(autoCommit);

        doThrow(dataUpdateException).when(fileStore).store(fileId, content, metadata, connection);

        try {
            transactionalFileStore.store(fileId, content, metadata);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to store file with id " + fileId));
            assertThat(expected.getCause(), is(dataUpdateException));
        }

        final InOrder inOrder = inOrder(
                fileStore,
                databaseConnectionUtils,
                closer);

        inOrder.verify(fileStore).store(fileId, content, metadata, connection);
        inOrder.verify(databaseConnectionUtils).rollback(connection);
        inOrder.verify(databaseConnectionUtils).setAutoCommit(autoCommit, connection);
        inOrder.verify(closer).close(connection);
    }

    @Test
    public void shouldFindAFileInTheDatabase() throws Exception {

        final UUID fileId = UUID.randomUUID();

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        final Optional<StorableFile> storableFile = of(mock(StorableFile.class));

        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(fileStore.find(fileId, connection)).thenReturn(storableFile);

        assertThat(transactionalFileStore.find(fileId), is(storableFile));

        final InOrder inOrder = inOrder(
                fileStore,
                closer);

        inOrder.verify(fileStore).find(fileId, connection);
        inOrder.verify(closer).close(connection);
    }

    @Test
    public void shouldAlwaysCloseTheConnectionIfTheFindFails() throws Exception {

        final DataUpdateException dataUpdateException = new DataUpdateException("Ooops");

        final UUID fileId = UUID.randomUUID();

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        final Optional<StorableFile> storableFile = of(mock(StorableFile.class));

        when(dataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(databaseConnectionUtils.getConnection(dataSource)).thenReturn(connection);

        when(fileStore.find(fileId, connection)).thenThrow(dataUpdateException);

        try {
            transactionalFileStore.find(fileId);
            fail();
        } catch (JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to find file with id " + fileId));
            assertThat(expected.getCause(), is(dataUpdateException));
        }

        final InOrder inOrder = inOrder(
                fileStore,
                closer);

        inOrder.verify(fileStore).find(fileId, connection);
        inOrder.verify(closer).close(connection);
    }
}
