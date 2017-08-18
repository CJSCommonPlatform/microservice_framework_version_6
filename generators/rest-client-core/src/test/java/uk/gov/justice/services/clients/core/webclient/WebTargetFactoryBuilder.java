package uk.gov.justice.services.clients.core.webclient;

import static uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProviderFactory.jndiBasedServiceContextNameProviderWith;

import uk.gov.justice.services.common.http.DefaultServerPortProvider;

public class WebTargetFactoryBuilder {

    private String appName;

    public static WebTargetFactoryBuilder aWebTargetFactoryBuilder() {
        return new WebTargetFactoryBuilder();
    }

    public WebTargetFactoryBuilder withAppName(final String appName) {
        this.appName = appName;
        return this;
    }

    public WebTargetFactory build() {

        final WebTargetFactory webTargetFactory = new WebTargetFactory();

        webTargetFactory.baseUriFactory = new BaseUriFactory();
        webTargetFactory.baseUriFactory.serverPortProvider = new DefaultServerPortProvider();
        webTargetFactory.baseUriFactory.mockServerPortProvider = new MockServerPortProvider();
        webTargetFactory.baseUriFactory.mockServerPortProvider.contextMatcher = new ContextMatcher();
        webTargetFactory.baseUriFactory.mockServerPortProvider.contextMatcher.contextNameProvider = jndiBasedServiceContextNameProviderWith(appName);

        return webTargetFactory;
    }
}
