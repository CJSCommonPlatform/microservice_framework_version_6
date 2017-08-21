package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.test.utils.persistence.AbstractJdbcRepositoryIT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventStreamJdbcRepositoryIT extends AbstractJdbcRepositoryIT<EventStreamJdbcRepository> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventStreamJdbcRepositoryIT.class);

    private static final UUID STREAM_ID = randomUUID();

    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    public EventStreamJdbcRepositoryIT() {
        super(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML);
    }

    @Before
    public void setUp() throws Exception {
        jdbcRepository = new EventStreamJdbcRepository();
        registerDataSource();
    }

    @Test
    public void shouldAddStreamsWithSequentialOrderNumbers() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();

        jdbcRepository.insert(new EventStream(streamId1));
        jdbcRepository.insert(new EventStream(streamId2));

        final List<EventStream> streams = jdbcRepository.findAll().collect(toList());
        assertThat(streams, hasSize(2));
        assertThat(streams.get(0).getStreamId(), is(streamId1));
        assertThat(streams.get(0).getSequenceNumber(), is(1L));

        assertThat(streams.get(1).getStreamId(), is(streamId2));
        assertThat(streams.get(1).getSequenceNumber(), is(2L));

    }

    @Test
    public void shouldReturnPageOfSpecifiedSize() {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();

        final Map<String, Object> params = new HashMap();

        jdbcRepository.insert(new EventStream(streamId1));
        jdbcRepository.insert(new EventStream(streamId2));
        jdbcRepository.insert(new EventStream(streamId3));
        jdbcRepository.insert(new EventStream(streamId4));

        final List<EventStream> streams = jdbcRepository.getPage(0, 3, params).collect(toList());
        assertThat(streams, hasSize(3));

    }

    @Test
    public void shouldReturnPageWithOffsetAndSize() {

        final UUID streamId5 = randomUUID();
        final UUID streamId6 = randomUUID();

        final Map<String, Object> params = new HashMap();

        jdbcRepository.insert(new EventStream(randomUUID(), 1L));
        jdbcRepository.insert(new EventStream(randomUUID(), 2L));
        jdbcRepository.insert(new EventStream(randomUUID(), 3L));
        jdbcRepository.insert(new EventStream(randomUUID(), 4L));
        jdbcRepository.insert(new EventStream(streamId5, 5L));
        jdbcRepository.insert(new EventStream(streamId6, 6L));
        jdbcRepository.insert(new EventStream(randomUUID(), 7L));

        final int offset = 4;
        final int pageSize = 2;
        final List<EventStream> streams = jdbcRepository.getPage(offset, pageSize, params).collect(toList());
        assertThat(streams, hasSize(2));

        assertThat(streams.get(0).getStreamId(), is(streamId5));
        assertThat(streams.get(0).getSequenceNumber(), is(5L));

        assertThat(streams.get(1).getStreamId(), is(streamId6));
        assertThat(streams.get(1).getSequenceNumber(), is(6L));


    }

}