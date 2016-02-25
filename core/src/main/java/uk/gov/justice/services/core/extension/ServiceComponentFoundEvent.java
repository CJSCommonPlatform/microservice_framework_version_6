package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Component;

import javax.enterprise.inject.spi.Bean;

public class ServiceComponentFoundEvent {

    private final Component component;
    private final Bean handlerBean;

    public ServiceComponentFoundEvent(final Component component, final Bean commandHandlerBean) {
        this.component = component;
        this.handlerBean = commandHandlerBean;
    }

    public Bean getHandlerBean() {
        return handlerBean;
    }

    public Component getComponent() {
        return component;
    }
}
