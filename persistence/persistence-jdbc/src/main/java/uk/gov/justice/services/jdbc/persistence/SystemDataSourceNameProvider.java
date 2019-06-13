package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.inject.Inject;

public class SystemDataSourceNameProvider {

    private static final String SYSTEM_JNDI_PATTERN = "java:/app/%s/DS.system";

    @Inject
    private JndiAppNameProvider jndiAppNameProvider;

    private String dataSourceName;

    public String getDataSourceName() {

        if(dataSourceName == null) {
            dataSourceName = format(SYSTEM_JNDI_PATTERN, contextName());
        }

        return dataSourceName;
    }

    private String contextName()  {

        final String warFileName = jndiAppNameProvider.getAppName();

        if (warFileName.contains("-")) {
            return warFileName.substring(0, warFileName.indexOf('-'));
        }

        return warFileName;
    }
}
