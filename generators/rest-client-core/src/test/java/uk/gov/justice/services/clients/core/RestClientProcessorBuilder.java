package uk.gov.justice.services.clients.core;

import static uk.gov.justice.services.clients.core.webclient.WebTargetFactoryBuilder.aWebTargetFactoryBuilder;

import uk.gov.justice.services.clients.core.webclient.WebTargetFactory;
import uk.gov.justice.services.clients.core.webclient.WebTargetFactoryBuilder;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

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
        restClientProcessor.jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        restClientProcessor.enveloper = new Enveloper(null);
        restClientProcessor.webTargetFactory = aWebTargetFactoryBuilder()
                .withAppName(appName)
                .build();

        return restClientProcessor;
    }
}
