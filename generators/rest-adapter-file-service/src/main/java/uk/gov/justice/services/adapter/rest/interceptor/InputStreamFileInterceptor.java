package uk.gov.justice.services.adapter.rest.interceptor;

import static uk.gov.justice.services.adapter.rest.multipart.FileInputDetails.FILE_INPUT_DETAILS_LIST;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class InputStreamFileInterceptor implements Interceptor {

    @Inject
    MultipleFileInputDetailsService multipleFileInputDetailsService;

    @Inject
    ResultsHandler resultsHandler;

    @Override
    @SuppressWarnings("unchecked")
    public InterceptorContext process(
            final InterceptorContext interceptorContext,
            final InterceptorChain interceptorChain) {

        final Optional<Object> inputParameterOptional = interceptorContext.getInputParameter(FILE_INPUT_DETAILS_LIST);

        if (inputParameterOptional.isPresent()) {

            final List<FileInputDetails> fileInputDetails = (List<FileInputDetails>) inputParameterOptional.get();
            final Map<String, UUID> results = multipleFileInputDetailsService.storeFileDetails(fileInputDetails);
            final JsonEnvelope inputEnvelope = resultsHandler.addResultsTo(interceptorContext.inputEnvelope(), results);

            return interceptorChain.processNext(interceptorContext.copyWithInput(inputEnvelope));
        }

        return interceptorChain.processNext(interceptorContext);
    }
}