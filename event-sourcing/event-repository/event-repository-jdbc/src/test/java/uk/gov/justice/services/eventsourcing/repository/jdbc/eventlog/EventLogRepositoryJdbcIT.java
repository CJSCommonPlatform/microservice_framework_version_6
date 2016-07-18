package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.EventLogRepositoryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;

import java.util.List;
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

    private static final UUID STREAM_ID = randomUUID();
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
        jdbcEventLogRepository.insert(eventLogOf(SEQUENCE_ID, STREAM_ID));
        jdbcEventLogRepository.insert(eventLogOf(SEQUENCE_ID + 1, STREAM_ID));
        jdbcEventLogRepository.insert(eventLogOf(SEQUENCE_ID + 2, STREAM_ID));

        Stream<EventLog> eventLogs = jdbcEventLogRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);
        Stream<EventLog> eventLogs2 = jdbcEventLogRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID + 1);
        Long latestSequenceId = jdbcEventLogRepository.getLatestSequenceIdForStream(STREAM_ID);

        assertThat(eventLogs.count(), equalTo(3L));
        assertThat(eventLogs2.count(), equalTo(2L));
        assertThat(latestSequenceId, equalTo(7L));
    }

    @Test
    public void shouldReturnEventsByStreamIdOrderedBySequenceId() throws InvalidSequenceIdException {
        jdbcEventLogRepository.insert(eventLogOf(1, randomUUID()));
        jdbcEventLogRepository.insert(eventLogOf(7, STREAM_ID));
        jdbcEventLogRepository.insert(eventLogOf(4, STREAM_ID));
        jdbcEventLogRepository.insert(eventLogOf(2, STREAM_ID));

        Stream<EventLog> eventLogs = jdbcEventLogRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);

        final List<EventLog> eventLogList = eventLogs.collect(toList());
        assertThat(eventLogList, hasSize(3));
        assertThat(eventLogList.get(0).getSequenceId(), is(2l));
        assertThat(eventLogList.get(1).getSequenceId(), is(4l));
        assertThat(eventLogList.get(2).getSequenceId(), is(7l));

    }

    @Test
    public void shouldReturnEventsByStreamIdFromSequenceIdOrderBySequenceId() throws InvalidSequenceIdException {
        jdbcEventLogRepository.insert(eventLogOf(5, randomUUID()));
        jdbcEventLogRepository.insert(eventLogOf(7, STREAM_ID));
        jdbcEventLogRepository.insert(eventLogOf(4, STREAM_ID));
        jdbcEventLogRepository.insert(eventLogOf(3, STREAM_ID));

        Stream<EventLog> eventLogs = jdbcEventLogRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, 4l);
        final List<EventLog> eventLogList = eventLogs.collect(toList());
        assertThat(eventLogList, hasSize(2));
        assertThat(eventLogList.get(0).getSequenceId(), is(4l));
        assertThat(eventLogList.get(1).getSequenceId(), is(7l));


    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateId() throws InvalidSequenceIdException {
        UUID id = randomUUID();
        jdbcEventLogRepository.insert(eventLogOf(id, SEQUENCE_ID));
        jdbcEventLogRepository.insert(eventLogOf(id, SEQUENCE_ID + 1));
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateSequenceId() throws InvalidSequenceIdException {
        jdbcEventLogRepository.insert(eventLogOf(SEQUENCE_ID, STREAM_ID));
        jdbcEventLogRepository.insert(eventLogOf(SEQUENCE_ID, STREAM_ID));
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

    private EventLog eventLogOf(UUID id, String name, UUID streamId, long sequenceId, String payloadJSON, String metadataJSON) {
        return new EventLog(id, streamId, sequenceId, name, metadataJSON, payloadJSON);
    }

    private EventLog eventLogOf(final long sequenceId,final UUID streamId) {
        return eventLogOf(randomUUID(), NAME, streamId, sequenceId, PAYLOAD_JSON, METADATA_JSON);
    }

    private EventLog eventLogOf(UUID id, long sequenceId) {
        return eventLogOf(id, NAME, STREAM_ID, sequenceId, PAYLOAD_JSON, METADATA_JSON);
    }

}