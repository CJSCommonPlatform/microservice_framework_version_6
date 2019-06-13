package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.inject.Inject;

public class ViewStoreDataSourceNameProvider {

    private static final String VIEW_STORE_JNDI_PATTERN = "java:/DS.%s";

    @Inject
    private JndiAppNameProvider jndiAppNameProvider;

    private String dataSourceName;

    public String getDataSourceName() {

        if(dataSourceName == null) {
            dataSourceName = format(VIEW_STORE_JNDI_PATTERN, contextName());
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
