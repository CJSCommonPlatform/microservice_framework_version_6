package uk.gov.justice.services.jmx.logging;

import static java.util.Optional.ofNullable;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.slf4j.MDC;

public class MdcLogger {

    @Inject
    private ServiceContextNameProvider serviceContextNameProvider;

    public void addServiceContextName() {
        ofNullable(serviceContextNameProvider.getServiceContextName())
                .ifPresent(value -> {
                    final String jsonAsString = createObjectBuilder().add(SERVICE_CONTEXT, value).build().toString();
                    MDC.put(REQUEST_DATA, jsonAsString);
                });
    }

    public void clearRequestData() {
        MDC.remove(REQUEST_DATA);
    }

    public Consumer<Runnable> mdcLoggerConsumer() {
        return runnable -> {
            try {
                addServiceContextName();
                runnable.run();
            } finally {
                clearRequestData();
            }
        };
    }
}
