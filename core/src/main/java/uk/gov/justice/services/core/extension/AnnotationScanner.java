package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.AnnotationLiteral;
import java.util.Optional;

/**
 * Scans all beans and processes framework specific annotations.
 */
public class AnnotationScanner implements Extension {

    @SuppressWarnings("unused")
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        beanManager.getBeans(Object.class, new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = -3118797828842400134L;
        }).stream().forEach(bean -> getEvent(bean).ifPresent(x -> beanManager.fireEvent(x)));

    }

    /**
     * Returns an event based on the handler type of the bean.
     *
     * @param bean a bean that could be a handler.
     * @return an optional event.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Optional<ServiceComponentFoundEvent> getEvent(final Bean bean) {
        if (bean.getBeanClass().isAnnotationPresent(ServiceComponent.class)) {
            return Optional.of(new ServiceComponentFoundEvent(Component.getComponentFromServiceComponent(bean.getBeanClass()), bean));
        } else {
            return Optional.empty();
        }
    }

}
