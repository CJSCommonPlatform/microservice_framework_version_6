package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)

public class EventInsertionStrategyProducerTest {

    private static final int INSERTED = 1;

    @Mock
    private Event event;

    @Mock
    private PreparedStatementWrapper preparedStatement;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventInsertionStrategyProducer strategyProducer;

    @Test
    public void shouldProducePostgresStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.eventsourcing.repository.jdbc.PostgresSQLEventLogInsertionStrategy";
        assertThat(strategyProducer.eventLogInsertionStrategy(), instanceOf(PostgresSQLEventLogInsertionStrategy.class));
    }

    @Test
    public void shouldProduceAnsiSQLStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy";
        assertThat(strategyProducer.eventLogInsertionStrategy(), instanceOf(AnsiSQLEventLogInsertionStrategy.class));
    }

    @Test
    public void shouldPassRepositoryToPostgresStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.eventsourcing.repository.jdbc.PostgresSQLEventLogInsertionStrategy";

        when(event.getCreatedAt()).thenReturn(new UtcClock().now());
        when(preparedStatement.executeUpdate()).thenReturn(INSERTED);

        strategyProducer.eventLogInsertionStrategy().insert(preparedStatement, event);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void shouldPassRepositoryToAnsiStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy";

        when(event.getCreatedAt()).thenReturn(new UtcClock().now());
        when(preparedStatement.executeUpdate()).thenReturn(INSERTED);

        strategyProducer.eventLogInsertionStrategy().insert(preparedStatement, event);
        verify(preparedStatement).executeUpdate();
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfClassDoesNotExist() {
        strategyProducer.strategyClass = "uk.gov.justice.services.eventsourcing.repository.jdbc.SomeUnknowClazzz";

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Could not instantiate event log insertion strategy");

        strategyProducer.eventLogInsertionStrategy();
    }
}