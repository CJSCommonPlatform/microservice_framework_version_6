package uk.gov.justice.services.jmx.bootstrap;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.bootstrap.blacklist.BlacklistedCommandsScanner;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;
import uk.gov.justice.services.jmx.command.SystemCommandStore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class SystemCommandHandlerScanner {

    private final SystemCommandProxyResolver systemCommandProxyResolver;
    private final CdiInstanceResolver cdiInstanceResolver;
    private final BlacklistedCommandsScanner blacklistedCommandsScanner;

    public SystemCommandHandlerScanner(
            final SystemCommandProxyResolver systemCommandProxyResolver,
            final CdiInstanceResolver cdiInstanceResolver,
            final BlacklistedCommandsScanner blacklistedCommandsScanner) {
        this.systemCommandProxyResolver = systemCommandProxyResolver;
        this.cdiInstanceResolver = cdiInstanceResolver;
        this.blacklistedCommandsScanner = blacklistedCommandsScanner;
    }

    public void registerSystemCommands(final BeanManager beanManager) {
        final Set<Bean<?>> cdiBeans = beanManager.getBeans(Object.class);

        final Set<SystemCommand> blacklistedCommands = blacklistedCommandsScanner.scanForBlacklistedCommands(
                cdiBeans,
                beanManager);

        final List<SystemCommandHandlerProxy> systemCommandHandlerProxies = cdiBeans.stream()
                .map(bean -> systemCommandProxyResolver.allCommandProxiesFor(bean, beanManager, blacklistedCommands))
                .flatMap(Collection::stream)
                .collect(toList());

        final SystemCommandStore systemCommandStore = cdiInstanceResolver.getInstanceOf(
                SystemCommandStore.class,
                beanManager
        );

        systemCommandStore.store(systemCommandHandlerProxies);
    }
}
