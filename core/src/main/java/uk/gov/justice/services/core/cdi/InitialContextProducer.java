package uk.gov.justice.services.core.cdi;

import javax.enterprise.inject.CreationException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@ApplicationScoped
public class InitialContextProducer {

    @Produces
    public InitialContext initialContext(@SuppressWarnings("unused") final InjectionPoint injectionPoint) {

        try {
            return new InitialContext();
        } catch (final NamingException e) {
            throw new CreationException("Failed to create an InitialContext", e);
        }
    }

    public void close(@Disposes InitialContext initialContext) {

        try {
            initialContext.close();
        } catch (final NamingException e) {
            throw new InjectionException("Failed to close InitialContext", e);
        }

    }
}
