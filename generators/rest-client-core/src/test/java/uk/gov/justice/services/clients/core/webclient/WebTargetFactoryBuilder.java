package uk.gov.justice.services.clients.core.webclient;

import uk.gov.justice.services.clients.core.DefaultServerPortProvider;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;

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
        webTargetFactory.baseUriFactory.defaultServerPortProvider = new DefaultServerPortProvider();
        webTargetFactory.baseUriFactory.mockServerPortProvider = new MockServerPortProvider();
        webTargetFactory.baseUriFactory.mockServerPortProvider.contextMatcher = new ContextMatcher();
        webTargetFactory.baseUriFactory.mockServerPortProvider.contextMatcher.contextNameProvider = new JndiBasedServiceContextNameProvider(appName);

        return webTargetFactory;
    }
}
