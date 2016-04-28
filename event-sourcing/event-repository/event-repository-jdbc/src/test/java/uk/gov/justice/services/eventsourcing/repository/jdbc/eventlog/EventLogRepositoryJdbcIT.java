package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.EventLogRepositoryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;

import java.util.UUID;
import java.util.stream.Stream;

import javax.naming.Context;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

public class EventLogRepositoryJdbcIT {

    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final Long SEQUENCE_ID = 5L;
    private static final String NAME = "Test Name";
    private static final String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private JdbcEventLogRepository jdbcEventLogRepository;

    private JdbcDataSource dataSource;

    @Before
    public void initializeDependencies() throws Exception {
        jdbcEventLogRepository = new JdbcEventLogRepository();
        registerDataSource();
    }

    @Test
    public void shouldStoreEventLogs() throws InvalidSequenceIdException {
        jdbcEventLogRepository.insert(createEventLogWithSequence(SEQUENCE_ID));
        jdbcEventLogRepository.insert(createEventLogWithSequence(SEQUENCE_ID + 1));
        jdbcEventLogRepository.insert(createEventLogWithSequence(SEQUENCE_ID + 2));

        Stream<EventLog> eventLogs = jdbcEventLogRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);
        Stream<EventLog> eventLogs2 = jdbcEventLogRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID + 1);
        Long latestSequenceId = jdbcEventLogRepository.getLatestSequenceIdForStream(STREAM_ID);

        assertThat(eventLogs.count(), equalTo(3L));
        assertThat(eventLogs2.count(), equalTo(2L));
        assertThat(latestSequenceId, equalTo(7L));
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateId() throws InvalidSequenceIdException {
        UUID id = UUID.randomUUID();
        jdbcEventLogRepository.insert(createEventLogWithSequence(id, SEQUENCE_ID));
        jdbcEventLogRepository.insert(createEventLogWithSequence(id, SEQUENCE_ID + 1));
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateSequenceId() throws InvalidSequenceIdException {
        jdbcEventLogRepository.insert(createEventLogWithSequence(SEQUENCE_ID));
        jdbcEventLogRepository.insert(createEventLogWithSequence(SEQUENCE_ID));
    }

    private void registerDataSource() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "org.apache.naming");

        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:./test;MV_STORE=FALSE;MVCC=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");
        jdbcEventLogRepository.datasource = dataSource;

        initDatabase();
    }

    private void initDatabase() throws Exception {
        Liquibase liquibase = new Liquibase(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }

    private EventLog createEventLog(UUID id, String name, UUID streamId, long sequenceId, String payloadJSON, String metadataJSON) {
        return new EventLog(id, streamId, sequenceId, name, metadataJSON, payloadJSON);
    }

    private EventLog createEventLogWithSequence(long sequenceId) {
        return createEventLog(UUID.randomUUID(), NAME, STREAM_ID, sequenceId, PAYLOAD_JSON, METADATA_JSON);
    }

    private EventLog createEventLogWithSequence(UUID id, long sequenceId) {
        return createEventLog(id, NAME, STREAM_ID, sequenceId, PAYLOAD_JSON, METADATA_JSON);
    }

}