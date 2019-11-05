package uk.gov.justice.services.jmx.command;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.framework.utilities.cdi.CdiProvider;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.bootstrap.blacklist.BlacklistedCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SystemCommandScanner {

    @Inject
    private CdiProvider cdiProvider;

    @Inject
    private CdiInstanceResolver cdiInstanceResolver;

    @Inject
    private BlacklistedCommands blacklistedCommands;

    private final List<SystemCommand> systemCommands = new ArrayList<>();

    public List<SystemCommand> findCommands() {

        if (systemCommands.isEmpty()) {
            systemCommands.addAll(doFindSystemCommands());
        }

        return systemCommands;
    }

    private List<SystemCommand> doFindSystemCommands() {
        final CDI<Object> cdi = cdiProvider.getCdi();
        final BeanManager beanManager = cdi.getBeanManager();
        final Set<Bean<?>> beans = beanManager.getBeans(SystemCommand.class);

        final List<SystemCommand> blackListedCommands = blacklistedCommands.getBlackListedCommands();

        return beans.stream()
                .map(bean -> (SystemCommand) cdiInstanceResolver.getInstanceOf(bean.getBeanClass(), beanManager))
                .filter(systemCommand -> ! blackListedCommands.contains(systemCommand))
                .collect(toList());
    }
}
