package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.persistence.TestEventStoreDataSourceFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;


public class EventBufferJdbcRepositoryIT {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    private EventBufferJdbcRepository jdbcRepository;

    @Before
    public void initDatabase() throws Exception {
        final TestEventStoreDataSourceFactory testEventStoreDataSourceFactory = new TestEventStoreDataSourceFactory(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML);
        final DataSource dataSource = testEventStoreDataSourceFactory.createDataSource();
        jdbcRepository = new EventBufferJdbcRepository(dataSource, new JdbcRepositoryHelper());

        try {
            final Poller poller = new Poller();

            poller.pollUntilFound(() -> {
                try {
                    dataSource.getConnection().prepareStatement("SELECT COUNT (*) FROM event_buffer;").execute();
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

    @Test
    public void shouldInsertAndReturnStreamOfEvents() {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();
        final String source = "source";

        jdbcRepository.insert(new EventBufferEvent(id1, 2L, "eventVersion_2", source));
        jdbcRepository.insert(new EventBufferEvent(id1, 1L, "eventVersion_1", source));
        jdbcRepository.insert(new EventBufferEvent(id1, 3L, "eventVersion_3", source));
        jdbcRepository.insert(new EventBufferEvent(id2, 1L, "eventVersion_1", source));

        final List<EventBufferEvent> events = jdbcRepository.findStreamByIdAndSource(id1, source)
                .collect(toList());

        assertThat(events, hasSize(3));

        assertThat(events.get(0).getStreamId(), is(id1));
        assertThat(events.get(0).getPosition(), is(1L));
        assertThat(events.get(0).getEvent(), is("eventVersion_1"));
        assertThat(events.get(0).getSource(), is(source));

        assertThat(events.get(1).getStreamId(), is(id1));
        assertThat(events.get(1).getPosition(), is(2L));
        assertThat(events.get(1).getEvent(), is("eventVersion_2"));
        assertThat(events.get(1).getSource(), is(source));

        assertThat(events.get(2).getStreamId(), is(id1));
        assertThat(events.get(2).getPosition(), is(3L));
        assertThat(events.get(2).getEvent(), is("eventVersion_3"));
        assertThat(events.get(2).getSource(), is(source));
    }

    @Test
    public void shouldNotReturnEventsIfTheyHaveADifferentSource() {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();
        final String source = "source";

        jdbcRepository.insert(new EventBufferEvent(id1, 2L, "eventVersion_2", "a-different-source"));
        jdbcRepository.insert(new EventBufferEvent(id1, 1L, "eventVersion_1", source));
        jdbcRepository.insert(new EventBufferEvent(id1, 3L, "eventVersion_3", source));
        jdbcRepository.insert(new EventBufferEvent(id2, 1L, "eventVersion_1", source));

        final List<EventBufferEvent> events = jdbcRepository.findStreamByIdAndSource(id1, source)
                .collect(toList());

        assertThat(events, hasSize(2));

        assertThat(events.get(0).getStreamId(), is(id1));
        assertThat(events.get(0).getPosition(), is(1L));
        assertThat(events.get(0).getEvent(), is("eventVersion_1"));
        assertThat(events.get(0).getSource(), is(source));

        assertThat(events.get(1).getStreamId(), is(id1));
        assertThat(events.get(1).getPosition(), is(3L));
        assertThat(events.get(1).getEvent(), is("eventVersion_3"));
        assertThat(events.get(1).getSource(), is(source));
    }

    @Test
    public void shouldRemoveFromBuffer() {
        final UUID id1 = randomUUID();
        final String source = "someOtherSource";
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id1, 2L, "someOtherEvent", source);

        jdbcRepository.insert(eventBufferEvent);

        assertThat(jdbcRepository.findStreamByIdAndSource(id1, source).collect(toList()), hasItem(eventBufferEvent));

        jdbcRepository.remove(eventBufferEvent);

        assertThat(jdbcRepository.findStreamByIdAndSource(id1, source).collect(toList()), empty());
    }
}
