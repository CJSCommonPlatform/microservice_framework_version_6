package uk.gov.justice.services.jmx.bootstrap;

public class ObjectFactory {

    public CdiInstanceResolver cdiInstanceResolver() {
        return new CdiInstanceResolver();
    }

    public SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory() {
        return new SystemCommandHandlerProxyFactory();
    }

    public SystemCommandProxyResolver systemCommandProxyResolver() {
        return new SystemCommandProxyResolver(
                cdiInstanceResolver(),
                systemCommandHandlerProxyFactory());
    }

    public SystemCommandScanner systemCommandScanner() {
        return new SystemCommandScanner(
                systemCommandProxyResolver(),
                cdiInstanceResolver());
    }
}
