package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.ServiceComponentLocation;

import javax.enterprise.inject.spi.Bean;

public class ServiceComponentFoundEvent {

    private final String componentName;
    private final Bean<?> handlerBean;
    private final ServiceComponentLocation location;

    public ServiceComponentFoundEvent(final String componentName, final Bean<?> handlerBean, final ServiceComponentLocation location) {
        this.componentName = componentName;
        this.handlerBean = handlerBean;
        this.location = location;
    }

    public Bean<?> getHandlerBean() {
        return handlerBean;
    }

    public String getComponentName() {
        return componentName;
    }

    public ServiceComponentLocation getLocation() {
        return location;
    }
}
