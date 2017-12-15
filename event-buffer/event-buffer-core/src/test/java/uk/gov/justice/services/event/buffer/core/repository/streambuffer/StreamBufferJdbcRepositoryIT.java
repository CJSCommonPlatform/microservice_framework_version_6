package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.persistence.TestDataSourceFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;


public class StreamBufferJdbcRepositoryIT {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    private TestDataSourceFactory testDataSourceFactory;

    private StreamBufferJdbcRepository jdbcRepository;

    @Before
    public void initDatabase() throws Exception {
        testDataSourceFactory = new TestDataSourceFactory(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML);
        jdbcRepository = new StreamBufferJdbcRepository(testDataSourceFactory.createDataSource(), new JdbcRepositoryHelper());
    }


    @Test
    public void shouldInsertAndReturnStreamOfdEvents() {
        final UUID id1 = randomUUID();

        final UUID id2 = randomUUID();

        jdbcRepository.insert(new StreamBufferEvent(id1, 2l, "someOtherEvent"));
        jdbcRepository.insert(new StreamBufferEvent(id1, 1l, "someEvent"));
        jdbcRepository.insert(new StreamBufferEvent(id1, 3l, "event"));
        jdbcRepository.insert(new StreamBufferEvent(id2, 1l, "anotherEvent"));

        List<StreamBufferEvent> events = jdbcRepository.streamById(id1)
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
        final Poller poller = new Poller();
        final UUID id1 = randomUUID();
        final StreamBufferEvent streamBufferEvent = new StreamBufferEvent(id1, 2L, "someOtherEvent");
        jdbcRepository.insert(streamBufferEvent);

        final Optional<Stream<StreamBufferEvent>> eventStream = poller.pollUntilFound(() -> {
            try {
                return Optional.of(jdbcRepository.streamById(id1));
            } catch (final JdbcRepositoryException e) {
                return Optional.empty();
            }
        });

        assertThat(eventStream.isPresent(), is(true));
        assertThat(eventStream.get().collect(toList()), hasItem(streamBufferEvent));

        jdbcRepository.remove(streamBufferEvent);

        final Optional<Boolean> streamIsEmpty = poller.pollUntilFound(() -> {
            if (jdbcRepository.streamById(id1).collect(toList()).isEmpty()) {
                return Optional.of(true);
            }

            return Optional.empty();
        });

        assertThat(streamIsEmpty.isPresent(), is((true)));
    }

}
