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

import org.junit.After;
import org.junit.Assert;

import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.core.messaging.Poller;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import uk.gov.justice.services.test.utils.persistence.TestDataSourceFactory;

public class EventStreamJdbcRepositoryIT {

    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final int PAGE_SIZE = 2;

    private EventStreamJdbcRepository jdbcRepository = new EventStreamJdbcRepository();

    @Before
    public void initialize() {
        try {
            jdbcRepository.dataSource = new TestDataSourceFactory(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML).createDataSource();
            jdbcRepository.logger = mock(Logger.class);
            jdbcRepository.eventStreamJdbcRepositoryHelper = new JdbcRepositoryHelper();

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


    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateStreamId() throws InvalidSequenceIdException {
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

        assertThat(streams.get(1).getStreamId(), is(streamId5));
        assertThat(streams.get(1).getSequenceNumber(), is(5L));

        assertThat(streams.get(2).getStreamId(), is(streamId4));
        assertThat(streams.get(2).getSequenceNumber(), is(4L));
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

}
