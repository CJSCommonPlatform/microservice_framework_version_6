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
 * Observes for {@link AfterDeploymentValidation} and adds all {@link InterceptorChainProvider}
 * implementations to the {@link InterceptorCache}
 */
public class InterceptorChainObserver implements Extension {

    private static final Logger LOGGER = getLogger(InterceptorChainObserver.class);

    private final List<Bean<?>> interceptorChainProviderBeans = new ArrayList<>();
    private final List<Bean<?>> interceptorBeans = new ArrayList<>();

    @SuppressWarnings({"unused"})
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        beanManager.getBeans(InterceptorChainProvider.class, AnyLiteral.create()).stream()
                .peek(this::logInteceptorChainProvider)
                .forEach(interceptorChainProviderBeans::add);

        beanManager.getBeans(Interceptor.class, AnyLiteral.create()).stream()
                .peek(this::logInteceptor)
                .forEach(interceptorBeans::add);
    }

    List<Bean<?>> getInterceptorChainProviderBeans() {
        return interceptorChainProviderBeans;
    }

    List<Bean<?>> getInterceptorBeans() {
        return interceptorBeans;
    }

    private void logInteceptorChainProvider(final Bean<?> bean) {
        LOGGER.info("Identified Interceptor Chain Provider {}", bean.getBeanClass().getSimpleName());
    }

    private void logInteceptor(final Bean<?> bean) {
        LOGGER.info("Identified Dispatcher Interceptor {}", bean.getBeanClass().getSimpleName());
    }
}