package uk.gov.justice.services.common.configuration;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

import com.google.common.annotations.VisibleForTesting;

@ApplicationScoped
public class JndiBasedServiceContextNameProvider implements ServiceContextNameProvider {

    @Resource(lookup = "java:app/AppName")
    String appName;

    public JndiBasedServiceContextNameProvider() {
    }

    @VisibleForTesting
    public JndiBasedServiceContextNameProvider(final String appName) {
        this.appName = appName;
    }

    @Override
    public String getServiceContextName() {
        return appName;
    }
}
