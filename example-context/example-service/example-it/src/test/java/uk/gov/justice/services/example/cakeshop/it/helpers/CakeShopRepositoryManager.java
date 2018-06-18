package uk.gov.justice.services.example.cakeshop.it.helpers;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.SnapshotJdbcRepository;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.StandaloneSnapshotJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventRepositoryFactory;

import javax.sql.DataSource;

public class CakeShopRepositoryManager {

    private final DatabaseManager databaseManager = new DatabaseManager();
    private final EventRepositoryFactory eventRepositoryFactory = new EventRepositoryFactory();
    private final StandaloneStreamStatusJdbcRepositoryFactory standaloneStreamStatusJdbcRepositoryFactory = new StandaloneStreamStatusJdbcRepositoryFactory();
    private final StandaloneSnapshotJdbcRepositoryFactory standaloneSnapshotJdbcRepositoryFactory = new StandaloneSnapshotJdbcRepositoryFactory();


    private EventJdbcRepository eventJdbcRepository;
    private StreamStatusJdbcRepository streamStatusJdbcRepository;
    private SnapshotJdbcRepository snapshotJdbcRepository;


    public void initialise() throws Exception {

        final DataSource eventStoreDataSource = databaseManager.initEventStoreDb();
        final DataSource viewStoreDatasource = databaseManager.initViewStoreDb();

        eventJdbcRepository = eventRepositoryFactory.getEventJdbcRepository(eventStoreDataSource);
        streamStatusJdbcRepository = standaloneStreamStatusJdbcRepositoryFactory.getSnapshotStreamStatusJdbcRepository(viewStoreDatasource);
        snapshotJdbcRepository = standaloneSnapshotJdbcRepositoryFactory.getSnapshotJdbcRepository(eventStoreDataSource);

        databaseManager.initFileServiceDb();
    }

    public EventJdbcRepository getEventJdbcRepository() {
        return eventJdbcRepository;
    }

    public StreamStatusJdbcRepository getStreamStatusJdbcRepository() {
        return streamStatusJdbcRepository;
    }

    public SnapshotJdbcRepository getSnapshotJdbcRepository() {
        return snapshotJdbcRepository;
    }
}
