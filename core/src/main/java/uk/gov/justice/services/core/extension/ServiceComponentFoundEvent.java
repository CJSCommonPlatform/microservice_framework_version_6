package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;

import javax.enterprise.inject.spi.Bean;

public class ServiceComponentFoundEvent {

    private final Component component;
    private final Bean<?> handlerBean;
    private final ServiceComponentLocation location;

    public ServiceComponentFoundEvent(final Component component, final Bean<?> handlerBean, final ServiceComponentLocation location) {
        this.component = component;
        this.handlerBean = handlerBean;
        this.location = location;
    }

    public Bean<?> getHandlerBean() {
        return handlerBean;
    }

    public Component getComponent() {
        return component;
    }

    public ServiceComponentLocation getLocation() {
        return location;
    }
}
