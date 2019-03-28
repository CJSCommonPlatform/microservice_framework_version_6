package uk.gov.justice.services.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.management.ObjectName;

public class MBeanFactory {

    private static final String SHUTTERING_DOMAIN_NAME = "shuttering";
    private static final String CATCHUP_DOMAIN_NAME = "catchup";

    private static final String SHUTTERING_BEAN = "Shuttering";
    private static final String CATCHUP_BEAN = "Catchup";
    private static final String OBJECT_NAME_KEY = "type";

    @Inject
    private Shuttering shuttering;

    @Inject
    private Catchup catchup;

    @Inject
    private ObjectNameFactory objectNameFactory;

    public Map<Object, ObjectName> createMBeans() {

        final Map<Object, ObjectName> mbeanMap = new HashMap<>();

        mbeanMap.put(shuttering, objectNameFactory.create(SHUTTERING_DOMAIN_NAME, OBJECT_NAME_KEY, SHUTTERING_BEAN));
        mbeanMap.put(catchup, objectNameFactory.create(CATCHUP_DOMAIN_NAME, OBJECT_NAME_KEY, CATCHUP_BEAN));

        return mbeanMap;
    }

}
