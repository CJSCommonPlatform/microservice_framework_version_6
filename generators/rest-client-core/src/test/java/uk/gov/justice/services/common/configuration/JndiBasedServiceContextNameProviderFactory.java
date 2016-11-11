package uk.gov.justice.services.common.configuration;

public class JndiBasedServiceContextNameProviderFactory {

    public static JndiBasedServiceContextNameProvider jndiBasedServiceContextNameProviderWith(final String appName) {
        final JndiBasedServiceContextNameProvider nameProvider = new JndiBasedServiceContextNameProvider();
        nameProvider.appName = appName;
        return nameProvider;
    }
}