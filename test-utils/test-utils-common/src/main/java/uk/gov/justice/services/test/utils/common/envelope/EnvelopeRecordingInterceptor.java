package uk.gov.justice.services.test.utils.common.envelope;


import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EnvelopeRecordingInterceptor extends TestEnvelopeRecorder implements Interceptor {
    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        record(interceptorContext.inputEnvelope());
        return interceptorChain.processNext(interceptorContext);
    }
}