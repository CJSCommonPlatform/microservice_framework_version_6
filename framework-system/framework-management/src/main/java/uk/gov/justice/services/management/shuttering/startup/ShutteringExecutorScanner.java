package uk.gov.justice.services.management.shuttering.startup;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

public class ShutteringExecutorScanner implements Extension {

    private final CdiInstanceResolver cdiInstanceResolver;

    public ShutteringExecutorScanner() {
        this(new CdiInstanceResolver());
    }

    public ShutteringExecutorScanner(final CdiInstanceResolver cdiInstanceResolver) {
        this.cdiInstanceResolver = cdiInstanceResolver;
    }

    public void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        final ShutteringRegistry shutteringRegistry = cdiInstanceResolver.getInstanceOf(
                ShutteringRegistry.class,
                beanManager);

        final Set<Bean<?>> beans = beanManager.getBeans(Object.class);

        beans.stream()
                .map(Bean::getBeanClass)
                .filter(beanClass -> beanClass.isAnnotationPresent(ShutteringExecutor.class))
                .forEach(shutteringRegistry::registerAsShutterable);
    }
}
