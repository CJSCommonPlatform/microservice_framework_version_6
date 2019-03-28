package uk.gov.justice.services.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.management.ObjectName;

@ApplicationScoped
public class MBeanRegistry {

    @Inject
    private MBeanFactory mBeanFactory;

    private Map<Object, ObjectName> mbeanMap = new HashMap<>();

    public Map<Object, ObjectName> getMBeanMap() {

        if (mbeanMap.isEmpty()) {
            mbeanMap = mBeanFactory.createMBeans();
        }

        return mbeanMap;
    }
}
