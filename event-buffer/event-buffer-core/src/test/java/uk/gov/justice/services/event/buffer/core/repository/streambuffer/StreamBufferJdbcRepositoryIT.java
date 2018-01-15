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
import uk.gov.justice.services.test.utils.persistence.TestDataSourceFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;


public class StreamBufferJdbcRepositoryIT {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    private StreamBufferJdbcRepository jdbcRepository;

    @Before
    public void initDatabase() throws Exception {
        final TestDataSourceFactory testDataSourceFactory = new TestDataSourceFactory(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML);
        final JdbcDataSource dataSource = testDataSourceFactory.createDataSource();
        jdbcRepository = new StreamBufferJdbcRepository(dataSource, new JdbcRepositoryHelper());

        try {
            final Poller poller = new Poller();

            poller.pollUntilFound(() -> {
                try {
                    dataSource.getConnection().prepareStatement("SELECT COUNT (*) FROM stream_buffer;").execute();
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
    public void shouldInsertAndReturnStreamOfdEvents() {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();

        jdbcRepository.insert(new StreamBufferEvent(id1, 2L, "someOtherEvent"));
        jdbcRepository.insert(new StreamBufferEvent(id1, 1L, "someEvent"));
        jdbcRepository.insert(new StreamBufferEvent(id1, 3L, "event"));
        jdbcRepository.insert(new StreamBufferEvent(id2, 1L, "anotherEvent"));

        final List<StreamBufferEvent> events = jdbcRepository.streamById(id1)
                .collect(toList());

        assertThat(events, hasSize(3));

        assertThat(events.get(0).getStreamId(), is(id1));
        assertThat(events.get(0).getVersion(), is(1L));
        assertThat(events.get(0).getEvent(), is("someEvent"));

        assertThat(events.get(1).getStreamId(), is(id1));
        assertThat(events.get(1).getVersion(), is(2L));
        assertThat(events.get(1).getEvent(), is("someOtherEvent"));

        assertThat(events.get(2).getStreamId(), is(id1));
        assertThat(events.get(2).getVersion(), is(3L));
        assertThat(events.get(2).getEvent(), is("event"));
    }

    @Test
    public void shouldRemoveFromBuffer() {
        final UUID id1 = randomUUID();
        final StreamBufferEvent streamBufferEvent = new StreamBufferEvent(id1, 2L, "someOtherEvent");

        jdbcRepository.insert(streamBufferEvent);

        assertThat(jdbcRepository.streamById(id1).collect(toList()), hasItem(streamBufferEvent));

        jdbcRepository.remove(streamBufferEvent);

        assertThat(jdbcRepository.streamById(id1).collect(toList()), empty());
    }
}
