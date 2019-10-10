package uk.gov.justice.services.jmx.bootstrap.blacklist;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.jmx.api.command.EventCatchupCommand.CATCHUP;
import static uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand.INDEXER_CATCHUP;
import static uk.gov.justice.services.jmx.api.command.PingCommand.PING;
import static uk.gov.justice.services.jmx.api.command.RebuildCommand.REBUILD;
import static uk.gov.justice.services.jmx.api.command.ShutterCommand.SHUTTER;
import static uk.gov.justice.services.jmx.api.command.UnshutterCommand.UNSHUTTER;

import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;
import uk.gov.justice.services.jmx.api.command.PingCommand;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BlacklistedCommandsFilterTest {

    @InjectMocks
    private BlacklistedCommandsFilter blacklistedCommandsFilter;

    @Test
    public void shouldReturnTrueIfTheHandlesSystemCommandNameIdDoesNotMatchAnyOfTheNamesOfTheBlacklistedSystemCommands() throws Exception {

        final Set<SystemCommand> blacklistedCommands = newHashSet(
                new ShutterCommand(),
                new UnshutterCommand(),
                new EventCatchupCommand(),
                new PingCommand(),
                new RebuildCommand(),
                new IndexerCatchupCommand()
        );

        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(SHUTTER, blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(UNSHUTTER, blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(CATCHUP, blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(PING, blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(REBUILD, blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(INDEXER_CATCHUP, blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed("SOME_OTHER_COMMAND", blacklistedCommands), is(true));
    }
}
