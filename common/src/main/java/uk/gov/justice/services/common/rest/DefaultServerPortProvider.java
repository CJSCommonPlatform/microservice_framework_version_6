package uk.gov.justice.services.common.rest;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultServerPortProvider implements ServerPortProvider {

    static final String DEFAULT_PORT = "DEFAULT_PORT";

    public String getDefaultPort() {
        return System.getProperty(DEFAULT_PORT, "8080");
    }
}
