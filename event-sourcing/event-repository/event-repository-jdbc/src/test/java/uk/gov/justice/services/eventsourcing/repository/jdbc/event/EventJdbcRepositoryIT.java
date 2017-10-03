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

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.test.utils.persistence.AbstractJdbcRepositoryIT;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventJdbcRepositoryIT extends AbstractJdbcRepositoryIT<EventJdbcRepository> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventJdbcRepository.class);

    private static final UUID STREAM_ID = randomUUID();
    private static final Long SEQUENCE_ID = 5L;
    private static final String NAME = "Test Name";
    private static final String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private final static ZonedDateTime TIMESTAMP = new UtcClock().now();


    public EventJdbcRepositoryIT() {
        super(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML);
    }

    @Before
    public void initializeDependencies() throws Exception {
        jdbcRepository = new EventJdbcRepository();
        jdbcRepository.logger = LOGGER;
        jdbcRepository.eventInsertionStrategy = new AnsiSQLEventLogInsertionStrategy();
        registerDataSource();
    }

    @Test
    public void shouldStoreEventsUsingInsert() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventOf(SEQUENCE_ID, STREAM_ID));
        jdbcRepository.insert(eventOf(SEQUENCE_ID + 1, STREAM_ID));
        jdbcRepository.insert(eventOf(SEQUENCE_ID + 2, STREAM_ID));

        final Stream<Event> events = jdbcRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);
        final Stream<Event> events2 = jdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID + 1);
        final Long latestSequenceId = jdbcRepository.getLatestSequenceIdForStream(STREAM_ID);

        assertThat(events.count(), equalTo(3L));
        assertThat(events2.count(), equalTo(2L));
        assertThat(latestSequenceId, equalTo(7L));
    }

    @Test
    public void shouldReturnEventsByStreamIdOrderedBySequenceId() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventOf(1, randomUUID()));
        jdbcRepository.insert(eventOf(7, STREAM_ID));
        jdbcRepository.insert(eventOf(4, STREAM_ID));
        jdbcRepository.insert(eventOf(2, STREAM_ID));

        final Stream<Event> events = jdbcRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(3));
        assertThat(eventList.get(0).getSequenceId(), is(2L));
        assertThat(eventList.get(1).getSequenceId(), is(4L));
        assertThat(eventList.get(2).getSequenceId(), is(7L));
    }

    @Test
    public void shouldStoreAndReturnDateCreated() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventOf(1, STREAM_ID));

        Stream<Event> events = jdbcRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(1));
        assertThat(eventList.get(0).getCreatedAt(), is(TIMESTAMP));
    }

    @Test
    public void shouldReturnEventsByStreamIdFromSequenceIdOrderBySequenceId() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventOf(5, randomUUID()));
        jdbcRepository.insert(eventOf(7, STREAM_ID));
        jdbcRepository.insert(eventOf(4, STREAM_ID));
        jdbcRepository.insert(eventOf(3, STREAM_ID));

        final Stream<Event> events = jdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, 4L);
        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(2));
        assertThat(eventList.get(0).getSequenceId(), is(4L));
        assertThat(eventList.get(1).getSequenceId(), is(7L));
    }

    @Test
    public void shouldReturnAllEventsOrderedBySequenceId() throws InvalidSequenceIdException {
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
    public void shouldThrowExceptionOnDuplicateId() throws InvalidSequenceIdException {
        final UUID id = randomUUID();
        jdbcRepository.insert(eventOf(id, SEQUENCE_ID));
        jdbcRepository.insert(eventOf(id, SEQUENCE_ID + 1));
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateSequenceId() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventOf(SEQUENCE_ID, STREAM_ID));
        jdbcRepository.insert(eventOf(SEQUENCE_ID, STREAM_ID));
    }

    @Test
    public void shouldReturnPageOfSpecifiedSize()
            throws InvalidSequenceIdException, SQLException {

        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 5l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final List<Event> streams = jdbcRepository.head(streamId, 3).collect(toList());
        assertThat(streams, hasSize(3));

    }

    @Test
    public void shouldReturnFeedFromOffsetIncludingAndSize() throws InvalidSequenceIdException, SQLException {

        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final List<Event> streams = jdbcRepository.forward(streamId, 4L, 2L).collect(toList());
        assertThat(streams, hasSize(2));

        assertThat(streams.get(0).getStreamId(), is(streamId));
        assertThat(streams.get(0).getSequenceId(), is(5L));

        assertThat(streams.get(1).getStreamId(), is(streamId));
        assertThat(streams.get(1).getSequenceId(), is(4L));
    }

    @Test
    public void shouldReturnPageOnlyOfRequiredStreamWithOffsetAndSize() throws InvalidSequenceIdException, SQLException {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId2));
        }

        for (long sequence = 1; sequence < 9l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId1));
        }

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId3));
        }

        final List<Event> streams = jdbcRepository.forward(streamId1, 4L, 3L).collect(toList());
        assertThat(streams, hasSize(3));

        assertThat(streams.get(0).getStreamId(), is(streamId1));
        assertThat(streams.get(0).getSequenceId(), is(6L));

        assertThat(streams.get(1).getStreamId(), is(streamId1));
        assertThat(streams.get(1).getSequenceId(), is(5L));

        assertThat(streams.get(2).getStreamId(), is(streamId1));
        assertThat(streams.get(2).getSequenceId(), is(4L));

    }

    @Test
    public void shouldReturnTrueWhenRecordExists() throws InvalidSequenceIdException {
        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final boolean exists = jdbcRepository.recordExists(streamId, 7);
        assertTrue(exists);
    }

    @Test
    public void shouldReturnFalseWhenRecordDoesNotExist() throws InvalidSequenceIdException {
        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final boolean exists = jdbcRepository.recordExists(streamId, 10);
        assertFalse(exists);
    }

    @Test
    public void shouldReturnHeadRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final List<Event> streams = jdbcRepository.head(streamId, 2).collect(toList());
        assertThat(streams, hasSize(2));

        assertThat(streams.get(0).getStreamId(), is(streamId));
        assertThat(streams.get(0).getSequenceId(), is(7L));

        assertThat(streams.get(1).getStreamId(), is(streamId));
        assertThat(streams.get(1).getSequenceId(), is(6L));
    }

    @Test
    public void shouldReturnNextRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final List<Event> streams = jdbcRepository.backward(streamId, 3L, 2).collect(toList());
        assertThat(streams, hasSize(2));

        assertThat(streams.get(0).getStreamId(), is(streamId));
        assertThat(streams.get(0).getSequenceId(), is(3L));

        assertThat(streams.get(1).getStreamId(), is(streamId));
        assertThat(streams.get(1).getSequenceId(), is(2L));
    }

    @Test
    public void shouldReturnPreviousRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final List<Event> streams = jdbcRepository.forward(streamId, 3L, 2).collect(toList());
        assertThat(streams, hasSize(2));

        assertThat(streams.get(0).getStreamId(), is(streamId));
        assertThat(streams.get(0).getSequenceId(), is(4L));

        assertThat(streams.get(1).getStreamId(), is(streamId));
        assertThat(streams.get(1).getSequenceId(), is(3L));
    }


    @Test
    public void shouldReturnFirstRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId = randomUUID();

        for (long sequence = 1; sequence < 8l; sequence++) {
            jdbcRepository.insert(eventOf(sequence, streamId));
        }

        final List<Event> streams = jdbcRepository.first(streamId, 2).collect(toList());
        assertThat(streams, hasSize(2));

        assertThat(streams.get(0).getStreamId(), is(streamId));
        assertThat(streams.get(0).getSequenceId(), is(2L));

        assertThat(streams.get(1).getStreamId(), is(streamId));
        assertThat(streams.get(1).getSequenceId(), is(1L));
    }

    private Event eventOf(final UUID id, final String name, final UUID streamId, final long sequenceId, final String payloadJSON, final String metadataJSON, final ZonedDateTime timestamp) {
        return new Event(id, streamId, sequenceId, name, metadataJSON, payloadJSON, timestamp);
    }

    private Event eventOf(final long sequenceId, final UUID streamId) {
        return eventOf(randomUUID(), NAME, streamId, sequenceId, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
    }

    private Event eventOf(final UUID id, final long sequenceId) {
        return eventOf(id, NAME, STREAM_ID, sequenceId, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
    }
}