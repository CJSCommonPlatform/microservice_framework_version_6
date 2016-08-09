package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.test.utils.persistence.AbstractJdbcRepositoryIT;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;


public class StreamBufferJdbcRepositoryIT extends AbstractJdbcRepositoryIT<StreamBufferJdbcRepository> {
    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    public StreamBufferJdbcRepositoryIT() {
        super(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML);
    }

    @Before
    public void initializeDependencies() throws Exception {
        jdbcRepository = new StreamBufferJdbcRepository();
        registerDataSource();
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
        assertThat(events.get(0).getVersion(), is(1l));
        assertThat(events.get(0).getEvent(), is("someEvent"));

        assertThat(events.get(1).getStreamId(), is(id1));
        assertThat(events.get(1).getVersion(), is(2l));
        assertThat(events.get(1).getEvent(), is("someOtherEvent"));

        assertThat(events.get(2).getStreamId(), is(id1));
        assertThat(events.get(2).getVersion(), is(3l));
        assertThat(events.get(2).getEvent(), is("event"));

    }


    @Test
    public void shouldRemoveFromBuffer() {
        final UUID id1 = randomUUID();
        final StreamBufferEvent streamBufferEvent = new StreamBufferEvent(id1, 2l, "someOtherEvent");
        jdbcRepository.insert(streamBufferEvent);

        assertThat(jdbcRepository.streamById(id1).collect(toList()), hasItem(streamBufferEvent));

        jdbcRepository.remove(streamBufferEvent);

        assertThat(jdbcRepository.streamById(id1).collect(toList()), empty());

    }

}
