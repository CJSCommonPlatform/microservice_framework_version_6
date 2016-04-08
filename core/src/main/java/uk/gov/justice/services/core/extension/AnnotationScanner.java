package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Event;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.justice.services.core.annotation.Component.componentFromServiceComponent;

/**
 * Scans all beans and processes framework specific annotations.
 */
public class AnnotationScanner implements Extension {

    private List<Object> events = Collections.synchronizedList(new ArrayList<>());

    @SuppressWarnings("unused")
    <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat) {
        final AnnotatedType<T> annotatedType = pat.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(Event.class)) {
            events.add(new EventFoundEvent(annotatedType.getJavaClass(), annotatedType.getAnnotation(Event.class).value()));
        }
    }

    @SuppressWarnings("unused")
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        beanManager.getBeans(Object.class, annotationLiteral()).stream()
                .filter(b -> b.getBeanClass().isAnnotationPresent(ServiceComponent.class))
                .forEach(this::processServiceComponentsForEvents);

        fireAllCollectedEvents(beanManager);
    }

    private void fireAllCollectedEvents(BeanManager beanManager) {
        events.stream().forEach(beanManager::fireEvent);
    }

    private AnnotationLiteral<Any> annotationLiteral() {
        return new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = -3118797828842400134L;
        };
    }

    /**
     * Processes bean for annotations and adds events to the list.
     *
     * @param bean a bean that has an annotation and could be of interest to the framework wiring.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processServiceComponentsForEvents(final Bean bean) {
        final Component component = componentFromServiceComponent(bean.getBeanClass());
        final Object event = bean.getBeanClass().isAnnotationPresent(Remote.class) ?
                new RemoteServiceComponentFoundEvent(component, bean) :
                new ServiceComponentFoundEvent(component, bean);
        events.add(event);
    }
}
