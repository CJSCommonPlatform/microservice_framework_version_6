package uk.gov.justice.services.jmx.command;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.framework.utilities.cdi.CdiProvider;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

public class SystemCommandScanner {

    @Inject
    private CdiProvider cdiProvider;

    @Inject
    private CdiInstanceResolver cdiInstanceResolver;

    public List<SystemCommand> findCommands() {

        final CDI<Object> cdi = cdiProvider.getCdi();
        final BeanManager beanManager = cdi.getBeanManager();
        final Set<Bean<?>> beans = beanManager.getBeans(SystemCommand.class);

        return beans.stream()
                .map(bean -> (SystemCommand) cdiInstanceResolver.getInstanceOf(bean.getBeanClass(), beanManager))
                .collect(toList());

    }
}
