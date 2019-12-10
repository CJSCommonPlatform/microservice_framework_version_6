package uk.gov.justice.services.system.persistence;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.system.domain.StoredCommand;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class StoredCommandRepositoryIT {

    @Mock
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @SuppressWarnings("unused")
    @Spy
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @SuppressWarnings("unused")
    @Spy
    private JdbcResultSetStreamer jdbcResultSetStreamer = new JdbcResultSetStreamer();

    @Mock
    private Logger logger;

    @InjectMocks
    private StoredCommandRepository storedCommandRepository;


    @Test
    public void shouldInsertAndGetShutteredCommands() throws Exception {

        final DataSource systemDataSource = new TestJdbcDataSourceProvider().getSystemDataSource("framework");

        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);

        storedCommandRepository.deleteAll();

        final ZonedDateTime now = new UtcClock().now();

        final StoredCommand storedCommand_1 = new StoredCommand(randomUUID(), "command envelope 1", "destination 1", now.plusSeconds(1));
        final StoredCommand storedCommand_2 = new StoredCommand(randomUUID(), "command envelope 2", "destination 2", now.plusSeconds(2));
        final StoredCommand storedCommand_3 = new StoredCommand(randomUUID(), "command envelope 3", "destination 3", now.plusSeconds(3));

        storedCommandRepository.save(storedCommand_1);
        storedCommandRepository.save(storedCommand_2);
        storedCommandRepository.save(storedCommand_3);

        final Stream<StoredCommand> shutteredCommandStream = storedCommandRepository.streamStoredCommands();

        final List<StoredCommand> storedCommands = shutteredCommandStream.collect(toList());

        shutteredCommandStream.close();

        assertThat(storedCommands.size(), is(3));

        assertThat(storedCommands, hasItem(storedCommand_1));
        assertThat(storedCommands, hasItem(storedCommand_2));
        assertThat(storedCommands, hasItem(storedCommand_3));
    }

    @Test
    public void shouldDeleteShutteredCommand() throws Exception {

        final DataSource systemDataSource = new TestJdbcDataSourceProvider().getSystemDataSource("framework");

        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);

        storedCommandRepository.deleteAll();

        final ZonedDateTime now = new UtcClock().now();

        final StoredCommand storedCommand_1 = new StoredCommand(randomUUID(), "command envelope 1", "destination 1", now.plusSeconds(1));
        final StoredCommand storedCommand_2 = new StoredCommand(randomUUID(), "command envelope 2", "destination 2", now.plusSeconds(2));
        final StoredCommand storedCommand_3 = new StoredCommand(randomUUID(), "command envelope 3", "destination 3", now.plusSeconds(3));

        storedCommandRepository.save(storedCommand_1);
        storedCommandRepository.save(storedCommand_2);
        storedCommandRepository.save(storedCommand_3);

        final Stream<StoredCommand> shutteredCommandStream = storedCommandRepository.streamStoredCommands();

        assertThat(shutteredCommandStream.count(), is(3L));

        shutteredCommandStream.close();

        storedCommandRepository.delete(storedCommand_2.getEnvelopeId());

        final Stream<StoredCommand> secondShutteredCommandStream = storedCommandRepository.streamStoredCommands();
        final List<StoredCommand> storedCommands = secondShutteredCommandStream.collect(toList());

        assertThat(storedCommands.size(), is(2));

        assertThat(storedCommands, hasItem(storedCommand_1));
        assertThat(storedCommands, hasItem(storedCommand_3));
    }

    @Test
    public void shouldBeIdempotentWhenSaving() throws Exception {

        final DataSource systemDataSource = new TestJdbcDataSourceProvider().getSystemDataSource("framework");

        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);

        storedCommandRepository.deleteAll();

        final UUID envelopeId = fromString("0b305815-f251-481d-9a69-2ff60c3a88ca");
        final StoredCommand storedCommand = new StoredCommand(envelopeId, "command envelope 1", "destination 1", new UtcClock().now());

        storedCommandRepository.save(storedCommand);


        final Stream<StoredCommand> secondShutteredCommandStream = storedCommandRepository.streamStoredCommands();
        final List<StoredCommand> storedCommands = secondShutteredCommandStream.collect(toList());

        assertThat(storedCommands.size(), is(1));

        assertThat(storedCommands, hasItem(storedCommand));

        storedCommandRepository.save(storedCommand);

        verify(logger).warn("Command with id '0b305815-f251-481d-9a69-2ff60c3a88ca' not inserted into stored_command table. Command with that id already exists");
    }
}
