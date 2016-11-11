package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Provider;

import javax.enterprise.inject.spi.Bean;

/**
 * Event representing the occurrence of a class with an {@link Provider} annotation having been
 * identified by the framework.
 */
public class ProviderFoundEvent {

    private final Bean<?> bean;

    public ProviderFoundEvent(final Bean<?> bean) {
        this.bean = bean;
    }

    public Bean<?> getBean() {
        return bean;
    }

}
