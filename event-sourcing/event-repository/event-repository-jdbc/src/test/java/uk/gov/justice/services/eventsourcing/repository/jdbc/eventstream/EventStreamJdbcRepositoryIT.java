package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;


import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.justice.services.test.utils.persistence.TestDataSourceFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class EventStreamJdbcRepositoryIT {

    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private EventStreamJdbcRepository jdbcRepository;
    private JdbcDataSource dataSource;


    @Before
    public void initialize() {
        try {

            jdbcRepository = new EventStreamJdbcRepository(new JdbcRepositoryHelper(), null, new UtcClock(), "tests", mock(Logger.class));
            dataSource = new TestDataSourceFactory(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML).createDataSource();
            ReflectionUtil.setField(jdbcRepository, "dataSource", dataSource);

            final Poller poller = new Poller();

            poller.pollUntilFound(() -> {
                try {
                    dataSource.getConnection().prepareStatement("SELECT COUNT (*) FROM event_stream;").execute();
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
        dataSource.getConnection().close();
    }

    @Test
    public void shouldStoreEventStreamUsingInsert() throws InvalidPositionException {
        jdbcRepository.insert(randomUUID());
        jdbcRepository.insert(randomUUID());
        jdbcRepository.insert(randomUUID());

        final Stream<EventStream> streamOfStreams = jdbcRepository.findAll();
        assertThat(streamOfStreams.count(), equalTo(3L));
    }

    @Test
    public void shouldNotThrowExceptionOnDuplicateStreamId() throws InvalidPositionException {
        final UUID streamID = randomUUID();
        jdbcRepository.insert(streamID);
        jdbcRepository.insert(streamID);
    }

    @Test
    public void shouldMarkStreamAsInactive() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId);

        final Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

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

        final Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());

        jdbcRepository.delete(streamId);

        assertFalse(jdbcRepository.findAll().findFirst().isPresent());
    }

    @Test
    public void shouldInsertNewStreamAsInactive() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId, false);

        final Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());
        assertFalse(eventStream.get().isActive());
    }
}
