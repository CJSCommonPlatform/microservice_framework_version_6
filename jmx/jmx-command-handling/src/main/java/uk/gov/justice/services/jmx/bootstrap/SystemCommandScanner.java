package uk.gov.justice.services.jmx.bootstrap;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;
import uk.gov.justice.services.jmx.command.SystemCommandStore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class SystemCommandScanner {

    private final SystemCommandProxyResolver systemCommandProxyResolver;
    private final CdiInstanceResolver cdiInstanceResolver;

    public SystemCommandScanner(final SystemCommandProxyResolver systemCommandProxyResolver, final CdiInstanceResolver cdiInstanceResolver) {
        this.systemCommandProxyResolver = systemCommandProxyResolver;
        this.cdiInstanceResolver = cdiInstanceResolver;
    }

    public void registerSystemCommands(final BeanManager beanManager) {
        final Set<Bean<?>> cdiBeans = beanManager.getBeans(Object.class);

        final List<SystemCommandHandlerProxy> systemCommandHandlerProxies = cdiBeans.stream()
                .map(bean -> systemCommandProxyResolver.allCommandProxiesFor(bean, beanManager))
                .flatMap(Collection::stream)
                .collect(toList());

        final SystemCommandStore systemCommandStore = cdiInstanceResolver.getInstanceOf(
                SystemCommandStore.class,
                beanManager
        );

        systemCommandStore.store(systemCommandHandlerProxies);
    }
}
