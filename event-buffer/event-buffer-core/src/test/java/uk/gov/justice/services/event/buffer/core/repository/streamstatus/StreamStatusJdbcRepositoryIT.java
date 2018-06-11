package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.persistence.TestDataSourceFactory;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;



public class StreamStatusJdbcRepositoryIT {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    private static final long INITIAL_VERSION = 0L;

    private StreamStatusJdbcRepository jdbcRepository;

    @Before
    public void initDatabase() throws Exception {
        final TestDataSourceFactory testDataSourceFactory = new TestDataSourceFactory(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML);
        final JdbcDataSource dataSource = testDataSourceFactory.createDataSource();
        jdbcRepository = new StreamStatusJdbcRepository(dataSource, new JdbcRepositoryHelper());

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
    public void shouldNotCreateSeparateInitialStreamStatusForTheNewSourceWhenWeHaveExistingEventsForTheStream() throws Exception {
        final String source = "unknown";
        final UUID streamId = randomUUID();

        initialiseBuffer(streamId, source);
        final StreamStatus streamStatus = new StreamStatus(streamId, 2L, source);

        jdbcRepository.update(streamStatus);

        initialiseBuffer(streamId, "sjp");
        final int count = jdbcRepository.countByStreamId(streamId);
        assertThat(count, is(1));

        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, "sjp");

        assertThat(result.get().getSource(), is("sjp"));
        assertThat(result.get().getVersion(), is(2L));
    }

    @Test
    public void shouldCreateSeparateInitialStreamStatusForTheNewSourceWhenWeHaveNoExistingEventsForTheStream() throws Exception {
        final String source = "sjp";
        final UUID streamId = randomUUID();

        initialiseBuffer(streamId, source);
        final int count = jdbcRepository.countByStreamId(streamId);
        assertThat(count, is(1));

        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, source);

        assertThat(result.get().getSource(), is(source));
        assertThat(result.get().getVersion(), is(0L));
    }

    @Test
    public void shouldAppendToExistingStreamStatusForTheNewSourceWhenWeHaveExistingEventsForTheStream() throws Exception {
        final String source = "sjp";
        final UUID streamId = randomUUID();

        initialiseBuffer(streamId, source);
        final StreamStatus streamStatus = new StreamStatus(streamId, 2L, source);

        jdbcRepository.update(streamStatus);

        initialiseBuffer(streamId, source);
        final int count = jdbcRepository.countByStreamId(streamId);
        assertThat(count, is(1));

        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, source);

        assertThat(result.get().getSource(), is("sjp"));
        assertThat(result.get().getVersion(), is(2L));
    }


        @Test
    public void shouldUpdateSourceWhenUnknown() throws Exception {
        final String source = "unknown";
        final UUID streamId = randomUUID();

        jdbcRepository.insert(streamStatusOf(streamId, 1L, source));

        final StreamStatus streamStatus = new StreamStatus(streamId, 2L, source);

        jdbcRepository.update(streamStatus);
        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, source);
        assertThat(result.get().getSource(), is(source));
        assertThat(result.get().getVersion(), is(2L));
    }

    @Test
    public void shouldUpdateSourceWhenNotUnknown() throws Exception {
        final UUID streamId = randomUUID();

        jdbcRepository.insert(streamStatusOf(streamId, 1L, "unknown"));

        final String source = "sjp";
        final StreamStatus streamStatus = new StreamStatus(streamId, 2L, source);

        jdbcRepository.update(streamStatus);
        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, source);
        assertThat(result.get().getSource(), is(source));
        assertThat(result.get().getVersion(), is(2L));
    }

    @Test
    public void shouldInsertAndReturnStreamStatus() throws Exception {
        final UUID id = randomUUID();
        final long version = 4L;
        final String source = "source";

        jdbcRepository.insert(streamStatusOf(id, version, source));

        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(id, source);
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(version));
        assertThat(result.get().getSource(), is(source));

    }

    @Test
    public void shouldReturnOptionalNotPresentIfStatusNotFound() throws Exception {
        Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(randomUUID(), "source");
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldUpdateVersionForSameStreamIdWithMultipleSources() throws Exception {
        final UUID id = randomUUID();
        jdbcRepository.insert(streamStatusOf(id, 4L, "source 4"));
        jdbcRepository.update(streamStatusOf(id, 5L, "source 4"));
        jdbcRepository.insert(streamStatusOf(id, 4L, "source 5"));
        jdbcRepository.update(streamStatusOf(id, 5L, "source 5"));

        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(id, "source 5");
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(5L));
        assertThat(result.get().getSource(), is("source 5"));

    }

    @Test
    public void shouldNotUpdateVersionForANewSourceField() throws Exception {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamStatusOf(streamId, 1L, "source2"));

        final String source3 = "source3";
        final StreamStatus streamStatus = new StreamStatus(streamId, 1L, source3);

        jdbcRepository.update(streamStatus);
        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, source3);
        assertThat(result,is(Optional.empty()));
    }

    @Test
    public void shouldUpdateVersionForAnExistingSourceField() throws Exception {
        final UUID streamId = randomUUID();
        final String source3 = "source3";
        jdbcRepository.insert(streamStatusOf(streamId, 4L, source3));
        jdbcRepository.insert(streamStatusOf(streamId, 1L, "source4"));

        final StreamStatus streamStatus = new StreamStatus(streamId, 5L, source3);

        jdbcRepository.update(streamStatus);
        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, source3);
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(5L));
        assertThat(result.get().getSource(), is(source3));
    }


    @Test
    public void shouldUpdateNewVersionNumberForExistingSourceWhenMultipleSourceEventsExist() throws Exception {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamStatusOf(streamId, 1L, "source1"));
        jdbcRepository.insert(streamStatusOf(streamId, 1L, "source2"));
        jdbcRepository.insert(streamStatusOf(streamId, 2L, "source3"));

        final String existingSource = "source2";
        final StreamStatus streamStatus = new StreamStatus(streamId, 2L, existingSource);

        jdbcRepository.update(streamStatus);
        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, existingSource);
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(2L));
        assertThat(result.get().getSource(), is(existingSource));
    }

    @Test
    public void shouldNotUpdateNewVersionNumberForNewSourceWhenMultipleSourceEventsExist() throws Exception {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamStatusOf(streamId, 1L, "source1"));
        jdbcRepository.insert(streamStatusOf(streamId, 1L, "source2"));
        jdbcRepository.insert(streamStatusOf(streamId, 2L, "source3"));

        final String newSource = "source4";
        final StreamStatus streamStatus = new StreamStatus(streamId, 1L, newSource);

        jdbcRepository.update(streamStatus);
        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(streamId, newSource);
        assertThat(result,is(Optional.empty()));
    }

    private StreamStatus streamStatusOf(final UUID id, final Long version, final String source) {
        return new StreamStatus(id, version, source);
    }

    private long initialiseBuffer(final UUID streamId, final String source) {
        jdbcRepository.updateSource(streamId,source);
        final Optional<StreamStatus> currentStatus = jdbcRepository.findByStreamIdAndSource(streamId, source);

        if (!currentStatus.isPresent()) {
            //this is to address race condition
            //in case of primary key violation the exception gets thrown, event goes back into topic and the transaction gets retried
            jdbcRepository
                    .insert(new StreamStatus(streamId, INITIAL_VERSION, source));
            return INITIAL_VERSION;

        } else {
            return currentStatus.get().getVersion();
        }
    }

}
