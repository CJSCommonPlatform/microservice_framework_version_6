package uk.gov.justice.services.common.configuration;

import static java.lang.String.format;
import static uk.gov.justice.services.common.configuration.CommonValueAnnotationDef.NULL_DEFAULT;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

abstract class AbstractValueProducer {
    private static final String GLOBAL_JNDI_NAME_PATTERN = "java:global/%s";

    InitialContext initialContext;

    protected AbstractValueProducer() throws NamingException {
        initialContext = new InitialContext();
    }

    protected String jndiValueFor(final CommonValueAnnotationDef annotation) throws NamingException {
        for (String jndiName : jndiNamesFrom(annotation)) {
            try {
                return (String) initialContext.lookup(jndiName);
            } catch (NameNotFoundException e) {
                //do nothing
            }
        }
        if (NULL_DEFAULT.equals(annotation.defaultValue())) {
            throw new MissingPropertyException(format("Missing property: %s", annotation.key()));
        }
        return annotation.defaultValue();

    }

    protected String globalJNDINameFrom(final CommonValueAnnotationDef annotation) {
        return format(GLOBAL_JNDI_NAME_PATTERN, annotation.key());
    }

    /**
     * The method is used to specify JNDI names to resolve. If resolution of a name fails
     * then the next name in order is picked for resolution
     *
     * @return jndi name(s) to resolve (in order of priority).
     */
    protected abstract String[] jndiNamesFrom(CommonValueAnnotationDef annotation);


}
