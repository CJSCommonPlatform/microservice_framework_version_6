package uk.gov.justice.services.event.stream.status.repository.jdbc;


import org.junit.Before;
import org.junit.Test;

import uk.gov.justice.services.test.utils.persistence.AbstractJdbcRepositoryIT;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.naming.NamingException;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class JdbcStreamStatusRepositoryIT extends AbstractJdbcRepositoryIT<JdbcStreamStatusRepository> {

    private final UUID RANDOM_UUID = UUID.randomUUID();

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    public JdbcStreamStatusRepositoryIT() {
        super(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML);
    }


    @Before
    public void initializeDependencies() throws Exception {
        jdbcRepository = new JdbcStreamStatusRepository();
        registerDataSource();
    }

    @Test
    public void shouldInsertAndFindStreamStatus() throws SQLException, NamingException {

        jdbcRepository.insert(streamStatusOf(RANDOM_UUID, 4L));
        Optional<StreamStatus> result = jdbcRepository.findByStreamId(RANDOM_UUID);
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(4L));

    }

    @Test
    public void shouldUpdateVersion() throws SQLException, NamingException {

        jdbcRepository.insert(streamStatusOf(RANDOM_UUID, 4L));
        jdbcRepository.update(streamStatusOf(RANDOM_UUID, 5L));

        Optional<StreamStatus> result = jdbcRepository.findByStreamId(RANDOM_UUID);
        assertTrue(result.isPresent());
        assertThat(result.get().getVersion(), is(5L));

    }

    private StreamStatus streamStatusOf(UUID id, Long version) {
        return new StreamStatus(id, version);
    }

}