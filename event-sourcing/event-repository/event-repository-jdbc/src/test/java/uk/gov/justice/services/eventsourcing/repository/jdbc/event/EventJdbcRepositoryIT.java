package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.persistence.TestDataSourceFactory;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class EventJdbcRepositoryIT {

    private static final UUID STREAM_ID = randomUUID();
    private static final Long SEQUENCE_ID = 5L;
    private static final String NAME = "Test Name";
    private static final String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private final static ZonedDateTime TIMESTAMP = new UtcClock().now();
    private final static String SOURCE = "source";


    private final EventJdbcRepository jdbcRepository = new EventJdbcRepository();

    @Before
    public void initialize() {
        try {
            jdbcRepository.dataSource = new TestDataSourceFactory(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML).createDataSource();
            jdbcRepository.logger = mock(Logger.class);
            jdbcRepository.eventInsertionStrategy = new AnsiSQLEventLogInsertionStrategy();
            jdbcRepository.jdbcRepositoryHelper = new JdbcRepositoryHelper();

            final Poller poller = new Poller();

            poller.pollUntilFound(() -> {
                try {
                    jdbcRepository.dataSource.getConnection().prepareStatement("SELECT COUNT (*) FROM event_log;").execute();
                    return Optional.of("Success");
                } catch (SQLException e) {
                    e.printStackTrace();
                    fail("EventJdbcRepository construction failed");
                    return Optional.empty();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            fail("EventJdbcRepository construction failed");
        }
    }

    @After
    public void after() throws SQLException {
        jdbcRepository.dataSource.getConnection().close();
    }

    @Test
    public void shouldStoreEventsUsingInsert() throws InvalidPositionException {
        jdbcRepository.insert(eventOf(SEQUENCE_ID, STREAM_ID));
        jdbcRepository.insert(eventOf(SEQUENCE_ID + 1, STREAM_ID));
        jdbcRepository.insert(eventOf(SEQUENCE_ID + 2, STREAM_ID));

        final Stream<Event> events = jdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID);
        final Stream<Event> events2 = jdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(STREAM_ID, SEQUENCE_ID + 1);
        final Long latestSequenceId = jdbcRepository.getStreamSize(STREAM_ID);

        assertThat(events.count(), equalTo(3L));
        assertThat(events2.count(), equalTo(2L));
        assertThat(latestSequenceId, equalTo(7L));
    }

    @Test
    public void shouldReturnEventsByStreamIdOrderedBySequenceId() throws InvalidPositionException {
        jdbcRepository.insert(eventOf(1, randomUUID()));
        jdbcRepository.insert(eventOf(7, STREAM_ID));
        jdbcRepository.insert(eventOf(4, STREAM_ID));
        jdbcRepository.insert(eventOf(2, STREAM_ID));

        final Stream<Event> events = jdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID);

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(3));
        assertThat(eventList.get(0).getSequenceId(), is(2L));
        assertThat(eventList.get(1).getSequenceId(), is(4L));
        assertThat(eventList.get(2).getSequenceId(), is(7L));
    }

    @Test
    public void shouldStoreAndReturnDateCreated() throws InvalidPositionException {
        jdbcRepository.insert(eventOf(1, STREAM_ID));

        Stream<Event> events = jdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID);

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(1));
        assertThat(eventList.get(0).getCreatedAt(), is(TIMESTAMP));
    }

    @Test
    public void shouldReturnEventsByStreamIdFromSequenceIdOrderBySequenceId() throws InvalidPositionException {
        jdbcRepository.insert(eventOf(5, randomUUID()));
        jdbcRepository.insert(eventOf(7, STREAM_ID));
        jdbcRepository.insert(eventOf(4, STREAM_ID));
        jdbcRepository.insert(eventOf(3, STREAM_ID));

        final Stream<Event> events = jdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(STREAM_ID, 4L);
        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(2));
        assertThat(eventList.get(0).getSequenceId(), is(4L));
        assertThat(eventList.get(1).getSequenceId(), is(7L));
    }

    @Test
    public void shouldReturnAllEventsOrderedBySequenceId() throws InvalidPositionException {
        jdbcRepository.insert(eventOf(1, randomUUID()));
        jdbcRepository.insert(eventOf(4, STREAM_ID));
        jdbcRepository.insert(eventOf(2, STREAM_ID));

        final Stream<Event> events = jdbcRepository.findAll();

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(3));
        assertThat(eventList.get(0).getSequenceId(), is(1L));
        assertThat(eventList.get(1).getSequenceId(), is(2L));
        assertThat(eventList.get(2).getSequenceId(), is(4L));
    }

    @Test
    public void shouldReturnStreamOfStreamIds() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        jdbcRepository.insert(eventOf(1, streamId1));
        jdbcRepository.insert(eventOf(1, streamId2));
        jdbcRepository.insert(eventOf(1, streamId3));
        jdbcRepository.insert(eventOf(2, streamId1));

        final Stream<UUID> streamIds = jdbcRepository.getStreamIds();

        final List<UUID> streamIdList = streamIds.collect(toList());

        assertThat(streamIdList, hasSize(3));
        assertThat(streamIdList, hasItem(streamId1));
        assertThat(streamIdList, hasItem(streamId2));
        assertThat(streamIdList, hasItem(streamId3));
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateId() throws InvalidPositionException {
        final UUID id = randomUUID();
        jdbcRepository.insert(eventOf(id, SEQUENCE_ID));
        jdbcRepository.insert(eventOf(id, SEQUENCE_ID + 1));
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateSequenceId() throws InvalidPositionException {
        jdbcRepository.insert(eventOf(SEQUENCE_ID, STREAM_ID));
        jdbcRepository.insert(eventOf(SEQUENCE_ID, STREAM_ID));
    }

    @Test
    public void shouldClearStream() throws InvalidPositionException {
        jdbcRepository.insert(eventOf(1, STREAM_ID));
        jdbcRepository.insert(eventOf(2, STREAM_ID));
        jdbcRepository.insert(eventOf(3, STREAM_ID));
        jdbcRepository.insert(eventOf(4, STREAM_ID));
        jdbcRepository.insert(eventOf(5, STREAM_ID));
        jdbcRepository.insert(eventOf(6, STREAM_ID));
        jdbcRepository.insert(eventOf(7, STREAM_ID));
        jdbcRepository.insert(eventOf(8, STREAM_ID));
        jdbcRepository.insert(eventOf(9, STREAM_ID));

        final Long latestSequenceId = jdbcRepository.getStreamSize(STREAM_ID);
        assertThat(latestSequenceId, equalTo(9L));

        jdbcRepository.clear(STREAM_ID);

        final Stream<Event> emptyEvents = jdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID);
        assertThat(emptyEvents.count(), equalTo(0L));

        final Long deletedStreamLatestSequenceId = jdbcRepository.getStreamSize(STREAM_ID);
        assertThat(deletedStreamLatestSequenceId, equalTo(0L));
    }

    private Event eventOf(final UUID id, final String name, final UUID streamId, final long sequenceId, final String payloadJSON, final String metadataJSON,
                          final ZonedDateTime timestamp, final String source) {
        return new Event(id, streamId, sequenceId, name, metadataJSON, payloadJSON, timestamp, source);
    }

    private Event eventOf(final long sequenceId, final UUID streamId) {
        return eventOf(randomUUID(), NAME, streamId, sequenceId, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP, SOURCE);
    }

    private Event eventOf(final UUID id, final long sequenceId) {
        return eventOf(id, NAME, STREAM_ID, sequenceId, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP, SOURCE);
    }
}