package uk.gov.justice.services.jmx.bootstrap.blacklist;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

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

        final SystemCommand_1 systemCommand_1 = new SystemCommand_1();
        final SystemCommand_2 systemCommand_2 = new SystemCommand_2();
        final SystemCommand_3 systemCommand_3 = new SystemCommand_3();

        final Set<SystemCommand> blacklistedCommands = newHashSet(
                systemCommand_1,
                systemCommand_2,
                systemCommand_3
        );

        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(systemCommand_1.getName(), blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(systemCommand_2.getName(), blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed(systemCommand_3.getName(), blacklistedCommands), is(false));
        assertThat(blacklistedCommandsFilter.isSystemCommandAllowed("SOME_OTHER_COMMAND", blacklistedCommands), is(true));
    }

}


