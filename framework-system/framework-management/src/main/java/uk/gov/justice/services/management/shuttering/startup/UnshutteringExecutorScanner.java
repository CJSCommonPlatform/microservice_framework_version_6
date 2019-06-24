package uk.gov.justice.services.management.shuttering.startup;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.shuttering.observers.unshuttering.UnshutteringRegistry;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

public class UnshutteringExecutorScanner implements Extension {

    private final CdiInstanceResolver cdiInstanceResolver;

    public UnshutteringExecutorScanner() {
        this(new CdiInstanceResolver());
    }

    public UnshutteringExecutorScanner(final CdiInstanceResolver cdiInstanceResolver) {
        this.cdiInstanceResolver = cdiInstanceResolver;
    }

    public void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        final UnshutteringRegistry unshutteringRegistry = cdiInstanceResolver.getInstanceOf(
                UnshutteringRegistry.class,
                beanManager);

        final Set<Bean<?>> beans = beanManager.getBeans(Object.class);

        beans.stream()
                .map(Bean::getBeanClass)
                .filter(beanClass -> beanClass.isAnnotationPresent(UnshutteringExecutor.class))
                .forEach(unshutteringRegistry::registerAsUnshutterable);
    }
}
