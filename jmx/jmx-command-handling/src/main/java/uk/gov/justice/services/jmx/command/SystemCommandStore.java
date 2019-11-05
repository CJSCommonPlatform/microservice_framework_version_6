package uk.gov.justice.services.jmx.command;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.List;

public interface SystemCommandStore {

    SystemCommandHandlerProxy findCommandProxy(final SystemCommand systemCommand);
    void store(final List<SystemCommandHandlerProxy> systemCommandProxies);
}
