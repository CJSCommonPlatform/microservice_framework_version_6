package uk.gov.justice.services.adapter.direct;

import static java.lang.String.format;

import uk.gov.justice.services.core.annotation.DirectAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

@ApplicationScoped
public class SynchronousDirectAdapterCache {

    private final Map<String, SynchronousDirectAdapter> adapters = new ConcurrentHashMap<>();

    @Inject
    private BeanManager beanManager;


    public SynchronousDirectAdapter directAdapterForComponent(final String component) {
        return adapters.computeIfAbsent(component, a -> adapterFromContext(component));
    }

    private SynchronousDirectAdapter adapterFromContext(final String component) {
        final Bean<?> bean = beanManager.getBeans(SynchronousDirectAdapter.class).stream()
                .filter(b -> component.equals(b.getBeanClass().getAnnotation(DirectAdapter.class).value()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(format("Direct adapter for component %s not found", component)));
        final CreationalContext ctx = beanManager.createCreationalContext(bean);
        return (SynchronousDirectAdapter) beanManager.getReference(bean, SynchronousDirectAdapter.class, ctx);
    }
}
