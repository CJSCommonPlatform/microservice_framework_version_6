package uk.gov.justice.services.jmx.bootstrap;

import uk.gov.justice.services.jmx.command.HandlerMethodValidator;

public class ObjectFactory {

    public CdiInstanceResolver cdiInstanceResolver() {
        return new CdiInstanceResolver();
    }

    public SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory() {
        return new SystemCommandHandlerProxyFactory();
    }

    public HandlerMethodValidator handlerMethodValidator() {
        return new HandlerMethodValidator();
    }

    public SystemCommandProxyResolver systemCommandProxyResolver() {
        return new SystemCommandProxyResolver(
                cdiInstanceResolver(),
                systemCommandHandlerProxyFactory(),
                handlerMethodValidator());
    }

    public SystemCommandScanner systemCommandScanner() {
        return new SystemCommandScanner(
                systemCommandProxyResolver(),
                cdiInstanceResolver());
    }
}
