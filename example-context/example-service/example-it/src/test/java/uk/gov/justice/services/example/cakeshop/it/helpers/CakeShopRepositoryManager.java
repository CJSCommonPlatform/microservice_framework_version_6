package uk.gov.justice.services.example.cakeshop.it.helpers;

import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.SnapshotJdbcRepository;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.StandaloneSnapshotJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventRepositoryFactory;

import javax.sql.DataSource;

public class CakeShopRepositoryManager {

    private final DatabaseManager databaseManager = new DatabaseManager();
    private final EventRepositoryFactory eventRepositoryFactory = new EventRepositoryFactory();
    private final StandaloneSubscriptionJdbcRepositoryFactory standaloneSubscriptionJdbcRepositoryFactory = new StandaloneSubscriptionJdbcRepositoryFactory();
    private final StandaloneSnapshotJdbcRepositoryFactory standaloneSnapshotJdbcRepositoryFactory = new StandaloneSnapshotJdbcRepositoryFactory();


    private EventJdbcRepository eventJdbcRepository;
    private SubscriptionJdbcRepository subscriptionJdbcRepository;
    private SnapshotJdbcRepository snapshotJdbcRepository;


    public void initialise() throws Exception {

        final DataSource eventStoreDataSource = databaseManager.initEventStoreDb();
        final DataSource viewStoreDatasource = databaseManager.initViewStoreDb();

        eventJdbcRepository = eventRepositoryFactory.getEventJdbcRepository(eventStoreDataSource);
        subscriptionJdbcRepository = standaloneSubscriptionJdbcRepositoryFactory.getSnapshotSubscriptionJdbcRepository(viewStoreDatasource);
        snapshotJdbcRepository = standaloneSnapshotJdbcRepositoryFactory.getSnapshotJdbcRepository(eventStoreDataSource);

        databaseManager.initFileServiceDb();
    }

    public EventJdbcRepository getEventJdbcRepository() {
        return eventJdbcRepository;
    }

    public SubscriptionJdbcRepository getSubscriptionJdbcRepository() {
        return subscriptionJdbcRepository;
    }

    public SnapshotJdbcRepository getSnapshotJdbcRepository() {
        return snapshotJdbcRepository;
    }
}
