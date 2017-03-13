package uk.gov.justice.services.adapter.rest.processor;


import static java.lang.String.format;

import uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

@ApplicationScoped
public class ResponseStrategyCache {

    private final Map<String, ResponseStrategy> strategies = new ConcurrentHashMap<>();

    @Inject
    private BeanManager beanManager;

    public ResponseStrategy responseStrategyOf(final String responseStrategyName) {
        return strategies.computeIfAbsent(responseStrategyName, s -> strategyFromContext(responseStrategyName));
    }

    private ResponseStrategy strategyFromContext(final String responseStrategyName) {
        final Bean bean = beanManager.getBeans(responseStrategyName).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Response response not found: %s", responseStrategyName)));
        final CreationalContext ctx = beanManager.createCreationalContext(bean);
        return (ResponseStrategy) beanManager.getReference(bean, ResponseStrategy.class, ctx);
    }
}