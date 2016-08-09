package uk.gov.justice.services.core.configuration;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

@ApplicationScoped
public class ValueProducer {

    @Inject
    AppNameProvider appNameProvider;

    InitialContext initialContext;

    public ValueProducer() throws NamingException {
        initialContext = new InitialContext();
    }

    @Value
    @Produces
    public String produceValue(final InjectionPoint ip) throws NamingException {
        return getValue(ip.getAnnotated().getAnnotation(Value.class));
    }

    private String getValue(final Value param) throws NamingException {
        try {
            return (String) initialContext.lookup(format("java:/app/%s/%s", appNameProvider.getAppName(), param.key()));
        } catch (NameNotFoundException e) {
            if (isEmpty(param.defaultValue())) {
                throw new MissingPropertyException(format("Missing property: %s", param.key()));
            }
            return param.defaultValue();
        }
    }

}
