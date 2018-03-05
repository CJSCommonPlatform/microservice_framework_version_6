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
    public void shouldUpdateVersion() throws Exception {
        final UUID id = randomUUID();
        jdbcRepository.insert(streamStatusOf(id, 4L, "source 4"));
        jdbcRepository.update(streamStatusOf(id, 5L, "source 5"));

        final Optional<StreamStatus> result = jdbcRepository.findByStreamIdAndSource(id, "source 5");
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(5L));
        assertThat(result.get().getSource(), is("source 5"));

    }

    private StreamStatus streamStatusOf(final UUID id, final Long version, final String source) {
        return new StreamStatus(id, version, source);
    }

}
