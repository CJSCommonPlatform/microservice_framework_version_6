package uk.gov.justice.services.common.configuration;

import static java.lang.String.format;
import static uk.gov.justice.services.common.configuration.CommonValueAnnotationDef.globalValueAnnotationOf;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.NamingException;

/**
 * Looks up global jndi names in order to inject their values into @GlobalValue annotated properties.
 */
@ApplicationScoped
public class GlobalValueProducer extends AbstractValueProducer {
    public GlobalValueProducer() throws NamingException {
        super();
    }

    @GlobalValue
    @Produces
    public String stringValueOf(final InjectionPoint ip) throws NamingException {
        return jndiValueFor(globalValueAnnotationOf(ip));
    }

    @GlobalValue
    @Produces
    public long longValueOf(final InjectionPoint ip) throws NamingException {
        return Long.valueOf(stringValueOf(ip));
    }

    @Override
    protected String jndiNameFrom(final CommonValueAnnotationDef annotation) {
        return format("java:global/%s", annotation.key());
    }
}
