package uk.gov.justice.services.clients.core;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultServerPortProvider {

    static final String DEFAULT_PORT = "DEFAULT_PORT";

    public String getDefaultPort() {
        return System.getProperty(DEFAULT_PORT, "8080");
    }
}
