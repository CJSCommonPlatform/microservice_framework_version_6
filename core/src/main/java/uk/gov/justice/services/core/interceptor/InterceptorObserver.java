package uk.gov.justice.services.core.interceptor;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.annotation.AnyLiteral;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.slf4j.Logger;

/**
 * Observes for {@link AfterDeploymentValidation} and adds all {@link Interceptor} implementations
 * to the {@link InterceptorCache}
 */
public class InterceptorObserver implements Extension {

    private static final Logger LOGGER = getLogger(InterceptorObserver.class);

    private final List<Bean<?>> interceptorBeans = new ArrayList<>();

    @SuppressWarnings({"unchecked", "unused"})
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        beanManager.getBeans(Interceptor.class, AnyLiteral.create()).stream()
                .peek(this::log)
                .forEach(interceptorBeans::add);
    }

    public List<Bean<?>> getInterceptorBeans() {
        return interceptorBeans;
    }

    private void log(final Bean<?> bean) {
        LOGGER.info("Identified Dispatcher Interceptor {}", bean.getBeanClass().getSimpleName());
    }
}
