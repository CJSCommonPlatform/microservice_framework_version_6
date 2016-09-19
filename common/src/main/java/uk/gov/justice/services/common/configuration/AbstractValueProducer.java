package uk.gov.justice.services.common.configuration;

import static java.lang.String.format;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

abstract class AbstractValueProducer {
    InitialContext initialContext;

    protected AbstractValueProducer() throws NamingException {
        initialContext = new InitialContext();
    }

    protected String jndiValueFor(final CommonValueAnnotationDef annotation) throws NamingException {
        try {
            return (String) initialContext.lookup(jndiNameFrom(annotation));
        } catch (NameNotFoundException e) {
            if (CommonValueAnnotationDef.NULL_DEFAULT.equals(annotation.defaultValue())) {
                throw new MissingPropertyException(format("Missing property: %s", annotation.key()));
            }
            return annotation.defaultValue();
        }
    }

    protected abstract String jndiNameFrom(CommonValueAnnotationDef annotation);
}
