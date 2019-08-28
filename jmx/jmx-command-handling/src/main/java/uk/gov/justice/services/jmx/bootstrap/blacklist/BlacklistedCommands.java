package uk.gov.justice.services.jmx.bootstrap.blacklist;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.List;

/**
 * Implement this class to return a List of SystemCommands that SHOULD NOT be handled by your context
 */
public interface BlacklistedCommands {

    List<SystemCommand> getBlackListedCommands();
}
