package uk.gov.justice.services.common.configuration;

import static java.lang.String.format;
import static uk.gov.justice.services.common.configuration.CommonValueAnnotationDef.localValueAnnotationOf;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.NamingException;


/**
 * Looks up context specific jndi names in order to inject their values into @Value annotated
 * properties.
 */
@ApplicationScoped
public class ValueProducer extends AbstractValueProducer {
    private static final String LOCAL_JNDI_NAME_PATTERN = "java:/app/%s/%s";

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    public ValueProducer() throws NamingException {
        super();
    }

    @Value
    @Produces
    public String stringValueOf(final InjectionPoint ip) throws NamingException {
        return jndiValueFor(localValueAnnotationOf(ip));
    }

    @Value
    @Produces
    public long longValueOf(final InjectionPoint ip) throws NamingException {
        return Long.valueOf(stringValueOf(ip));
    }

    @Override
    protected String[] jndiNamesFrom(final CommonValueAnnotationDef annotation) {
        return new String[]{localJNDINameFrom(annotation), globalJNDINameFrom(annotation)};
    }

    private String localJNDINameFrom(final CommonValueAnnotationDef annotation) {
        return format(LOCAL_JNDI_NAME_PATTERN, serviceContextNameProvider.getServiceContextName(), annotation.key());
    }


}
