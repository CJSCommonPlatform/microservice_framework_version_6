package uk.gov.justice.services.event.buffer.core.repository.streamstatus;


import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.persistence.TestDataSourceFactory;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;


public class StreamStatusJdbcRepositoryIT {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    private TestDataSourceFactory testDataSourceFactory;

    private StreamStatusJdbcRepository jdbcRepository;

    @Before
    public void initDatabase() throws Exception {
        testDataSourceFactory = new TestDataSourceFactory(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML);
        jdbcRepository = new StreamStatusJdbcRepository();
        jdbcRepository.dataSource = testDataSourceFactory.createDataSource();
        jdbcRepository.jdbcRepositoryHelper = new JdbcRepositoryHelper();
    }


    @Test
    public void shouldInsertAndReturnStreamStatus() throws SQLException, NamingException {
        final UUID id = UUID.randomUUID();
        jdbcRepository.insert(streamStatusOf(id, 4L));
        Optional<StreamStatus> result = jdbcRepository.findByStreamId(id);
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(4L));

    }

    @Test
    public void shouldReturnOptionalNotPresentIfStatusNotFound() throws SQLException, NamingException {
        Optional<StreamStatus> result = jdbcRepository.findByStreamId(UUID.randomUUID());
        assertFalse(result.isPresent());


    }

    @Test
    public void shouldUpdateVersion() throws SQLException, NamingException {
        final UUID id = UUID.randomUUID();
        jdbcRepository.insert(streamStatusOf(id, 4L));
        jdbcRepository.update(streamStatusOf(id, 5L));

        Optional<StreamStatus> result = jdbcRepository.findByStreamId(id);
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(5L));

    }

    private StreamStatus streamStatusOf(UUID id, Long version) {
        return new StreamStatus(id, version);
    }

}