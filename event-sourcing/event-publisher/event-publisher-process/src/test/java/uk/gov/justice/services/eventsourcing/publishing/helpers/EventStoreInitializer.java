package uk.gov.justice.services.eventsourcing.publishing.helpers;

import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class EventStoreInitializer {

    public void initializeEventStore(final DataSource eventStoreDataSource) throws LiquibaseException, SQLException {
        final Liquibase liquibase = new Liquibase(
                "liquibase/event-store-db-changelog.xml",
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(eventStoreDataSource.getConnection()));

        liquibase.dropAll();
        liquibase.update("");
    }
}
