package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.fieldValue;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventJdbcRepositoryFactoryTest {

    @Mock
    private EventInsertionStrategy eventInsertionStrategy;

    @Mock
    private JdbcRepositoryHelper jdbcRepositoryHelper;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Test
    public void shouldProduceEventJdbcRepository() throws Exception {
        final String jndiDatasource = "java:/app/example/DS.eventstore";

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource);

        assertThat(eventJdbcRepository, is(notNullValue()));

        final Optional<Object> eventInsertionStrategyField = fieldValue(eventJdbcRepository, "eventInsertionStrategy");
        assertThat(eventInsertionStrategyField, is(Optional.of(eventInsertionStrategy)));

        final Optional<Object> jdbcRepositoryHelperField = fieldValue(eventJdbcRepository, "jdbcRepositoryHelper");
        assertThat(jdbcRepositoryHelperField, is(Optional.of(jdbcRepositoryHelper)));

        final Optional<Object> jdbcDataSourceProviderField = fieldValue(eventJdbcRepository, "jdbcDataSourceProvider");
        assertThat(jdbcDataSourceProviderField, is(Optional.of(jdbcDataSourceProvider)));

        final Optional<Object> jndiDatasourceField = fieldValue(eventJdbcRepository, "jndiDatasource");
        assertThat(jndiDatasourceField, is(Optional.of(jndiDatasource)));

        final Optional<Object> loggerField = fieldValue(eventJdbcRepository, "logger");
        assertThat(loggerField.isPresent(), is(true));
    }
}