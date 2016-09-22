package uk.gov.justice.services.common.configuration;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JndiBasedServiceContextNameProvider implements ServiceContextNameProvider {

    @Resource(lookup = "java:app/AppName")
    String appName;

    @Override
    public String getServiceContextName() {
        return appName;
    }
}
