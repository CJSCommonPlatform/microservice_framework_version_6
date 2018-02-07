package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;


import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class EventStreamJdbcRepositoryIT {

    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final int PAGE_SIZE = 2;

    private EventStreamJdbcRepository jdbcRepository = new EventStreamJdbcRepository();

    private final static ZonedDateTime TIMESTAMP = new UtcClock().now();

    @Before
    public void initialize() {
        try {
            jdbcRepository.dataSource = new TestDataSourceFactory(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML).createDataSource();
            jdbcRepository.logger = mock(Logger.class);
            jdbcRepository.eventStreamJdbcRepositoryHelper = new JdbcRepositoryHelper();
            jdbcRepository.clock = new UtcClock();

            final Poller poller = new Poller();

            poller.pollUntilFound(() -> {
                try {
                    jdbcRepository.dataSource.getConnection().prepareStatement("SELECT COUNT (*) FROM event_stream;").execute();
                    return Optional.of("Success");
                } catch (SQLException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail("EventStreamJdbcRepository construction failed");
        }
    }

    @After
    public void after() throws SQLException {
        jdbcRepository.dataSource.getConnection().close();
    }

    @Test
    public void shouldStoreEventStreamUsingInsert() throws InvalidSequenceIdException {
        jdbcRepository.insert(randomUUID());
        jdbcRepository.insert(randomUUID());
        jdbcRepository.insert(randomUUID());

        final Stream<EventStream> streamOfStreams = jdbcRepository.findAll();

        assertThat(streamOfStreams.count(), equalTo(3L));
    }


    @Test
    public void shouldNotThrowExceptionOnDuplicateStreamId() throws InvalidSequenceIdException {
        final UUID streamID = randomUUID();
        jdbcRepository.insert(streamID);
        jdbcRepository.insert(streamID);
    }

    @Test
    public void shouldReturnPageOfSpecifiedSize() throws InvalidSequenceIdException, SQLException {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();
        final UUID streamId6 = randomUUID();
        final UUID streamId7 = randomUUID();

        jdbcRepository.insert(streamId1);
        jdbcRepository.insert(streamId2);
        jdbcRepository.insert(streamId3);
        jdbcRepository.insert(streamId4);
        jdbcRepository.insert(streamId5);
        jdbcRepository.insert(streamId6);
        jdbcRepository.insert(streamId7);

        long sequenceNumber = 4L;
        int pageSize = 3;

        final List<EventStream> streams = jdbcRepository.forward(sequenceNumber, pageSize).collect(toList());
        assertThat(streams, hasSize(3));

        assertThat(streams.get(0).getStreamId(), is(streamId6));
        assertThat(streams.get(0).getSequenceNumber(), is(6L));
        assertThat(streams.get(0).isActive(), is(true));

        assertThat(streams.get(1).getStreamId(), is(streamId5));
        assertThat(streams.get(1).getSequenceNumber(), is(5L));
        assertThat(streams.get(1).isActive(), is(true));

        assertThat(streams.get(2).getStreamId(), is(streamId4));
        assertThat(streams.get(2).getSequenceNumber(), is(4L));
        assertThat(streams.get(2).isActive(), is(true));
    }

    @Test
    public void shouldReturnTrueWhenRecordExists() throws InvalidSequenceIdException {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        jdbcRepository.insert(streamId1);
        jdbcRepository.insert(streamId2);
        jdbcRepository.insert(streamId3);

        final boolean exists = jdbcRepository.recordExists(3L);
        assertTrue(exists);
    }

    @Test
    public void shouldReturnFalseWhenRecordDoesNotExist() throws InvalidSequenceIdException {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        jdbcRepository.insert(streamId1);
        jdbcRepository.insert(streamId2);
        jdbcRepository.insert(streamId3);

        final boolean exists = jdbcRepository.recordExists(4L);
        assertFalse(exists);
    }

    @Test
    public void shouldReturnHeadRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        jdbcRepository.insert(streamId1);
        jdbcRepository.insert(streamId2);
        jdbcRepository.insert(streamId3);

        final List<EventStream> streamOfStreams = jdbcRepository.head(PAGE_SIZE).collect(toList());
        assertThat(streamOfStreams, hasSize(2));

        assertThat(streamOfStreams.get(0).getStreamId(), is(streamId3));
        assertThat(streamOfStreams.get(0).getSequenceNumber(), is(3L));

        assertThat(streamOfStreams.get(1).getStreamId(), is(streamId2));
        assertThat(streamOfStreams.get(1).getSequenceNumber(), is(2L));
    }

    @Test
    public void shouldReturnNextRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        jdbcRepository.insert(streamId1);
        jdbcRepository.insert(streamId2);
        jdbcRepository.insert(streamId3);

        final List<EventStream> streamOfStreams = jdbcRepository.backward(3L, PAGE_SIZE).collect(toList());

        assertThat(streamOfStreams, hasSize(2));
        assertThat(streamOfStreams.get(0).getStreamId(), is(streamId3));
        assertThat(streamOfStreams.get(0).getSequenceNumber(), is(3L));
        assertThat(streamOfStreams.get(1).getStreamId(), is(streamId2));
        assertThat(streamOfStreams.get(1).getSequenceNumber(), is(2L));
    }

    @Test
    public void shouldReturnPreviousRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        jdbcRepository.insert(streamId1);
        jdbcRepository.insert(streamId2);
        jdbcRepository.insert(streamId3);

        final List<EventStream> streamOfStreams = jdbcRepository.forward(1L, PAGE_SIZE).collect(toList());
        assertThat(streamOfStreams, hasSize(2));

        assertThat(streamOfStreams.get(0).getStreamId(), is(streamId2));
        assertThat(streamOfStreams.get(0).getSequenceNumber(), is(2L));

        assertThat(streamOfStreams.get(1).getStreamId(), is(streamId1));
        assertThat(streamOfStreams.get(1).getSequenceNumber(), is(1L));
    }


    @Test
    public void shouldReturnFirstRecords() throws InvalidSequenceIdException, SQLException {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        jdbcRepository.insert(streamId1);
        jdbcRepository.insert(streamId2);
        jdbcRepository.insert(streamId3);

        final List<EventStream> streamOfStreams = jdbcRepository.first(PAGE_SIZE).collect(toList());
        assertThat(streamOfStreams, hasSize(2));

        assertThat(streamOfStreams.get(0).getStreamId(), is(streamId2));
        assertThat(streamOfStreams.get(0).getSequenceNumber(), is(2L));

        assertThat(streamOfStreams.get(1).getStreamId(), is(streamId1));
        assertThat(streamOfStreams.get(1).getSequenceNumber(), is(1L));
    }

    @Test
    public void shouldMarkStreamAsInactive() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId);

        Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());
        assertTrue(eventStream.get().isActive());

        jdbcRepository.markActive(streamId, false);

        assertFalse(jdbcRepository.findAll().findFirst().get().isActive());
    }

    @Test
    public void shouldFindActiveStreams() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId, false);

        assertThat(jdbcRepository.findAll().collect(toList()).size(), is(1));
        assertThat(jdbcRepository.findActive().collect(toList()).size(), is(0));

        jdbcRepository.markActive(streamId, true);
        assertThat(jdbcRepository.findActive().collect(toList()).size(), is(1));
    }

    @Test
    public void shouldDeleteStream() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId);

        Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());

        jdbcRepository.delete(streamId);

        assertFalse(jdbcRepository.findAll().findFirst().isPresent());
    }

    @Test
    public void shouldInsertNewStreamAsInactive() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId, false);

        Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());
        assertFalse(eventStream.get().isActive());
    }
}
