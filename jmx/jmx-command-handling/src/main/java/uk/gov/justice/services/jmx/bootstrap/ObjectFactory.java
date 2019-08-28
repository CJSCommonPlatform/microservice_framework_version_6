package uk.gov.justice.services.jmx.bootstrap;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.bootstrap.blacklist.BlacklistedCommandsFilter;
import uk.gov.justice.services.jmx.bootstrap.blacklist.BlacklistedCommandsScanner;
import uk.gov.justice.services.jmx.command.HandlerMethodValidator;

public class ObjectFactory {

    public CdiInstanceResolver cdiInstanceResolver() {
        return new CdiInstanceResolver();
    }

    public SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory() {
        return new SystemCommandHandlerProxyFactory();
    }

    public BlacklistedCommandsScanner blacklistedCommandsScanner() {
        return new BlacklistedCommandsScanner(cdiInstanceResolver());
    }

    public HandlerMethodValidator handlerMethodValidator() {
        return new HandlerMethodValidator();
    }

    public BlacklistedCommandsFilter blacklistedCommandsFilter() {
        return new BlacklistedCommandsFilter();
    }

    public SystemCommandProxyResolver systemCommandProxyResolver() {
        return new SystemCommandProxyResolver(
                cdiInstanceResolver(),
                systemCommandHandlerProxyFactory(),
                handlerMethodValidator(),
                blacklistedCommandsFilter());
    }

    public SystemCommandHandlerScanner systemCommandScanner() {
        return new SystemCommandHandlerScanner(
                systemCommandProxyResolver(),
                cdiInstanceResolver(),
                blacklistedCommandsScanner());
    }
}
