package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Component;

import javax.enterprise.inject.spi.Bean;


public abstract class BaseServiceComponentFoundEvent {
    private final Component component;
    private final Bean<Object> handlerBean;

    BaseServiceComponentFoundEvent(final Bean<Object> commandHandlerBean, final Component component) {
        this.handlerBean = commandHandlerBean;
        this.component = component;
    }

    public Bean<Object> getHandlerBean() {
        return handlerBean;
    }

    public Component getComponent() {
        return component;
    }
}
