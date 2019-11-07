package uk.gov.justice.services.jmx.bootstrap.blacklist;

import static java.util.Collections.emptyList;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(1)
public class EmptyBlackListedCommands implements BlacklistedCommands {

    @Override
    public List<SystemCommand> getBlackListedCommands() {
        return emptyList();
    }
}
