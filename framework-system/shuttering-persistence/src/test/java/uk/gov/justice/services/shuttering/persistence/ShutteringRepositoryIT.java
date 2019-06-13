package uk.gov.justice.services.shuttering.persistence;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.shuttering.domain.ShutteredCommand;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringRepositoryIT {

    @Mock
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @Spy
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @Spy
    private JdbcResultSetStreamer jdbcResultSetStreamer = new JdbcResultSetStreamer();

    @InjectMocks
    private ShutteringRepository shutteringRepository;


    @Test
    public void shouldInsertAndGetShutteredCommands() throws Exception {

        final DataSource systemDataSource = new TestJdbcDataSourceProvider().getSystemDataSource("framework");

        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);

        shutteringRepository.deleteAll();

        final ZonedDateTime now = new UtcClock().now();

        final ShutteredCommand shutteredCommand_1 = new ShutteredCommand(randomUUID(), "command envelope 1", "destination 1", now.plusSeconds(1));
        final ShutteredCommand shutteredCommand_2 = new ShutteredCommand(randomUUID(), "command envelope 2", "destination 2", now.plusSeconds(2));
        final ShutteredCommand shutteredCommand_3 = new ShutteredCommand(randomUUID(), "command envelope 3", "destination 3", now.plusSeconds(3));

        shutteringRepository.save(shutteredCommand_1);
        shutteringRepository.save(shutteredCommand_2);
        shutteringRepository.save(shutteredCommand_3);

        final Stream<ShutteredCommand> shutteredCommandStream = shutteringRepository.streamShutteredCommands();

        final List<ShutteredCommand> shutteredCommands = shutteredCommandStream.collect(toList());

        shutteredCommandStream.close();

        assertThat(shutteredCommands.size(), is(3));

        assertThat(shutteredCommands, hasItem(shutteredCommand_1));
        assertThat(shutteredCommands, hasItem(shutteredCommand_2));
        assertThat(shutteredCommands, hasItem(shutteredCommand_3));
    }

    @Test
    public void shouldDeleteShutteredCommand() throws Exception {

        final DataSource systemDataSource = new TestJdbcDataSourceProvider().getSystemDataSource("framework");

        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);

        shutteringRepository.deleteAll();

        final ZonedDateTime now = new UtcClock().now();

        final ShutteredCommand shutteredCommand_1 = new ShutteredCommand(randomUUID(), "command envelope 1", "destination 1", now.plusSeconds(1));
        final ShutteredCommand shutteredCommand_2 = new ShutteredCommand(randomUUID(), "command envelope 2", "destination 2", now.plusSeconds(2));
        final ShutteredCommand shutteredCommand_3 = new ShutteredCommand(randomUUID(), "command envelope 3", "destination 3", now.plusSeconds(3));

        shutteringRepository.save(shutteredCommand_1);
        shutteringRepository.save(shutteredCommand_2);
        shutteringRepository.save(shutteredCommand_3);

        final Stream<ShutteredCommand> shutteredCommandStream = shutteringRepository.streamShutteredCommands();

        assertThat(shutteredCommandStream.count(), is(3L));

        shutteredCommandStream.close();

        shutteringRepository.delete(shutteredCommand_2.getEnvelopeId());

        final Stream<ShutteredCommand> secondShutteredCommandStream = shutteringRepository.streamShutteredCommands();
        final List<ShutteredCommand> shutteredCommands = secondShutteredCommandStream.collect(toList());

        assertThat(shutteredCommands.size(), is(2));

        assertThat(shutteredCommands, hasItem(shutteredCommand_1));
        assertThat(shutteredCommands, hasItem(shutteredCommand_3));
    }
}
