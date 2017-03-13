package uk.gov.justice.services.adapter.rest.mutipart;

import static uk.gov.justice.services.adapter.rest.mutipart.FileInputDetails.FILE_INPUT_DETAILS_LIST;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileBasedInterceptorContextFactory {

    public InterceptorContext create(final List<FileInputDetails> fileInputDetails, final JsonEnvelope envelope) {
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope);
        interceptorContext.setInputParameter(FILE_INPUT_DETAILS_LIST, fileInputDetails);

        return interceptorContext;
    }
}
