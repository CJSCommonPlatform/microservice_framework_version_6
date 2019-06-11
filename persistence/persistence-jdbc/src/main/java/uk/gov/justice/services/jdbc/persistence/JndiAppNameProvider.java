package uk.gov.justice.services.jdbc.persistence;

import javax.annotation.Resource;

public class JndiAppNameProvider {

    @Resource(lookup = "java:app/AppName")
    private String appName;

    public String getAppName() {
        return appName;
    }
}
