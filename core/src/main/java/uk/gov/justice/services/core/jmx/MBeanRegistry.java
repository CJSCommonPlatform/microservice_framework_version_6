package uk.gov.justice.services.core.jmx;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


@ApplicationScoped
public class MBeanRegistry {

    private static final String SHUTTERING_DOMAIN_NAME = "shuttering";
    private static final String CATCHUP_DOMAIN_NAME = "catchup";

    private static final String SHUTTERING_BEAN = "Shuttering";
    private static final String CATCHUP_BEAN = "Catchup";

    private Map<Object, ObjectName> mbeanMap = new HashMap<>();

    public Map<Object, ObjectName> getMBeanMap() {
        try {
            if (mbeanMap.isEmpty()) {
                mbeanMap.put(new Shuttering(), new ObjectName(SHUTTERING_DOMAIN_NAME, "type", SHUTTERING_BEAN));
                mbeanMap.put(new Catchup(), new ObjectName(CATCHUP_DOMAIN_NAME, "type", CATCHUP_BEAN));
            }
            return mbeanMap;
        } catch (final MalformedObjectNameException exception) {
            throw new MBeanException(format("Unable to create MBean map: %s", exception.getMessage()));
        }
    }
}
