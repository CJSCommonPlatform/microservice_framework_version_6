package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.annotation.Resource;

public class JndiDataSourceNameProvider {

    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s/DS.eventstore";

    @Resource(lookup = "java:app/AppName")
    private String warFileName;

    public String jndiName() {
        return format(JNDI_DS_EVENT_STORE_PATTERN, warFileName);
    }
}
