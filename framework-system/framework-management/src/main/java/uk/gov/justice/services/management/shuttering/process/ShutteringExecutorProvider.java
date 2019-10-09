package uk.gov.justice.services.management.shuttering.process;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;

import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

public class ShutteringExecutorProvider {

    @Inject
    private CdiInstanceResolver cdiInstanceResolver;

    @Inject
    private BeanManager beanManager;

    public List<ShutteringExecutor> getShutteringExecutors() {

        return beanManager.getBeans(ShutteringExecutor.class).stream()
                .map(Bean::getBeanClass)
                .map(this::getInstance)
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private ShutteringExecutor getInstance(final Class<?> shutteringExecutorClass) {
        return cdiInstanceResolver.getInstanceOf((Class<ShutteringExecutor>) shutteringExecutorClass, beanManager);
    }
}
