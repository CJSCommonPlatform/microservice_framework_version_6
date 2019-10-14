package uk.gov.justice.services.jmx.logging;

import static java.util.Optional.ofNullable;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.slf4j.MDC;

public class MdcLoggerInterceptor {

    @Inject
    private ServiceContextNameProvider serviceContextNameProvider;

    @AroundInvoke
    public Object addRequestDataToMdc(final InvocationContext invocationContext) throws Exception {

        ofNullable(serviceContextNameProvider.getServiceContextName())
                .ifPresent(value -> {
                    final String jsonAsString = createObjectBuilder().add(SERVICE_CONTEXT, value).build().toString();
                    MDC.put(REQUEST_DATA, jsonAsString);
                });

        final Object result = invocationContext.proceed();

        MDC.remove(REQUEST_DATA);

        return result;
    }
}
