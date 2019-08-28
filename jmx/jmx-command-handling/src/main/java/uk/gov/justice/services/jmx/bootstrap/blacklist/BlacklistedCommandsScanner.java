package uk.gov.justice.services.jmx.bootstrap.blacklist;

import static java.util.stream.Collectors.toSet;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class BlacklistedCommandsScanner {

    private final CdiInstanceResolver cdiInstanceResolver;

    public BlacklistedCommandsScanner(final CdiInstanceResolver cdiInstanceResolver) {
        this.cdiInstanceResolver = cdiInstanceResolver;
    }

    public Set<SystemCommand> scanForBlacklistedCommands(final Set<Bean<?>> cdiBeans, final BeanManager beanManager) {

        return cdiBeans.stream()
                .filter(this::isInstanceOfBlacklistedCommands)
                .map(bean -> asSystemCommandsList(bean, beanManager))
                .flatMap(Collection::stream)
                .collect(toSet());

    }

    private boolean isInstanceOfBlacklistedCommands(final Bean<?> bean) {
        return BlacklistedCommands.class.isAssignableFrom(bean.getBeanClass());
    }

    private List<SystemCommand> asSystemCommandsList(final Bean<?> blacklistedCommandsBean, final BeanManager beanManager) {

        final Class<?> blacklistedCommandsClass = blacklistedCommandsBean.getBeanClass();

        final BlacklistedCommands blacklistedCommands = (BlacklistedCommands) cdiInstanceResolver.getInstanceOf(
                blacklistedCommandsClass,
                beanManager);

        return blacklistedCommands.getBlackListedCommands();
    }
}
