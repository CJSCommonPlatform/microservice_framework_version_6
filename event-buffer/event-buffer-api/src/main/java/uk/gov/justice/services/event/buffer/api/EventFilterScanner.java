package uk.gov.justice.services.event.buffer.api;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventFilterScanner implements Extension {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventFilterScanner.class);

    @SuppressWarnings("unused")
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        final Set<Bean<?>> eventFilters = beanManager.getBeans(EventFilter.class);

        LOGGER.info("***************  FOUND EVENT FILTERS: " + eventFilters.size());

        for (Bean<?> eventFilterBean : eventFilters) {

        }

//        eventFilters.stream()
//                .filter(EventFilter.class::isInstance)
//                .map(EventFilter.class::cast)
//                .peek(x->LOGGER.info("EVENT SIZE: " + x.getSupportedEvents().size()))
//                .forEach(ef -> beanManager.fireEvent(new EventFilterFoundEvent(((EventFilter)ef).getSupportedEvents())));
    }
}
