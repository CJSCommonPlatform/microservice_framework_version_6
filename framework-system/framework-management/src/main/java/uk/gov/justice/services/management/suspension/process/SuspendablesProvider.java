package uk.gov.justice.services.management.suspension.process;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.suspension.api.Suspendable;

import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

public class SuspendablesProvider {

    @Inject
    private CdiInstanceResolver cdiInstanceResolver;

    @Inject
    private BeanManager beanManager;

    public List<Suspendable> getSuspendables() {

        return beanManager.getBeans(Suspendable.class).stream()
                .map(Bean::getBeanClass)
                .map(this::getInstance)
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private Suspendable getInstance(final Class<?> shutteringExecutorClass) {
        return cdiInstanceResolver.getInstanceOf((Class<Suspendable>) shutteringExecutorClass, beanManager);
    }
}
