package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.fieldValue;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamJdbcRepositoryFactoryTest {

    @Mock
    private JdbcRepositoryHelper eventStreamJdbcRepositoryHelper;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Test
    public void shouldProduceEventStreamJdbcRepository() throws Exception {
        final String jndiDatasource = "java:/app/example/DS.eventstore";

        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);

        assertThat(eventStreamJdbcRepository, is(CoreMatchers.notNullValue()));

        final Optional<Object> eventStreamJdbcRepositoryHelperField = fieldValue(eventStreamJdbcRepository, "eventStreamJdbcRepositoryHelper");
        assertThat(eventStreamJdbcRepositoryHelperField, is(Optional.of(eventStreamJdbcRepositoryHelper)));

        final Optional<Object> jdbcDataSourceProviderField = fieldValue(eventStreamJdbcRepository, "jdbcDataSourceProvider");
        assertThat(jdbcDataSourceProviderField, is(Optional.of(jdbcDataSourceProvider)));

        final Optional<Object> jndiDatasourceField = fieldValue(eventStreamJdbcRepository, "jndiDatasource");
        assertThat(jndiDatasourceField, is(Optional.of(jndiDatasource)));

        final Optional<Object> loggerField = fieldValue(eventStreamJdbcRepository, "logger");
        assertThat(loggerField.isPresent(), is(true));
    }
}
