package uk.gov.justice.services.clients.core;

import static uk.gov.justice.services.clients.core.webclient.WebTargetFactoryBuilder.aWebTargetFactoryBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;

public class RestClientProcessorBuilder {

    private String appName;

    public static RestClientProcessorBuilder aRestClientProcessorBuilder() {
        return new RestClientProcessorBuilder();
    }

    public RestClientProcessorBuilder withAppName(final String appName) {
        this.appName = appName;
        return this;
    }

    public RestClientProcessor build() {
        final RestClientProcessor restClientProcessor = new RestClientProcessor();
        restClientProcessor.stringToJsonObjectConverter = new StringToJsonObjectConverter();
        restClientProcessor.jsonObjectEnvelopeConverter = new DefaultJsonObjectEnvelopeConverter();
        restClientProcessor.enveloper = createEnveloper();
        restClientProcessor.webTargetFactory = aWebTargetFactoryBuilder()
                .withAppName(appName)
                .build();

        return restClientProcessor;
    }
}
