package uk.gov.justice.services.jmx.api.name;

import static java.lang.String.format;

import javax.inject.Inject;
import javax.management.ObjectName;

public class CommandMBeanNameProvider {

    private static final String DOMAIN_NAME = "uk.gov.justice.services.framework.management";
    private static final String OBJECT_NAME_FORMAT = "%s-system-command-handler-mbean";
    private static final String TYPE = "type";

    @Inject
    private ObjectNameFactory objectNameFactory;

    public ObjectName create(final String contextName) {

        return objectNameFactory.create(
                DOMAIN_NAME,
                TYPE,
                format(OBJECT_NAME_FORMAT, contextName));
    }
}
