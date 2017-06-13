package uk.gov.justice.services.adapters.rest.generator;


import static org.mockito.Mockito.when;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.adapter.rest.mapping.ActionMapper;
import uk.gov.justice.services.adapter.rest.multipart.FileInputDetailsFactory;
import uk.gov.justice.services.adapter.rest.parameter.ParameterCollectionBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.ValidParameterCollectionBuilder;
import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.adapter.rest.processor.response.AcceptedStatusNoEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopeEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopePayloadEntityResponseStrategy;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseRestAdapterGeneratorTest extends BaseGeneratorTest {

    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";
    private static final String REST_PROCESSOR = "restProcessor";
    private static final String ACTION_MAPPER = "actionMapper";
    private static final String FILE_INPUT_DETAILS_FACTORY = "fileInputDetailsFactory";
    private static final String VALID_PARAMETER_COLLECTION_BUILDER_FACTORY_FIELD = "validParameterCollectionBuilderFactory";
    private static final String TRACE_LOGGER_FIELD = "traceLogger";
    private static final String HTTP_TRACE_LOGGER_HELPER_FIELD = "httpTraceLoggerHelper";
    private static final String LOGGER_FIELD = "logger";

    @Mock
    protected InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    protected ActionMapper actionMapper;

    @Mock
    protected RestProcessor restProcessor;

    @Mock
    protected OkStatusEnvelopeEntityResponseStrategy okStatusEnvelopeEntityResponseStrategy;

    @Mock
    protected OkStatusEnvelopePayloadEntityResponseStrategy okStatusEnvelopePayloadEntityResponseStrategy;

    @Mock
    protected AcceptedStatusNoEntityResponseStrategy acceptedStatusNoEntityResponseStrategy;

    @Mock
    protected FileInputDetailsFactory fileInputDetailsFactory;

    @Mock
    protected ParameterCollectionBuilderFactory validParameterCollectionBuilderFactory;

    @Mock
    protected TraceLogger traceLogger;

    @Mock
    protected Logger logger;

    @Mock
    protected HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Before
    public void before() {
        super.before();
        generator = new RestAdapterGenerator();
        try {
            setField(generator, LOGGER_FIELD, logger);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set mock logger on generator under test", e);
        }
    }

    protected Object getInstanceOf(final Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, REST_PROCESSOR, restProcessor);
        setField(resourceObject, INTERCEPTOR_CHAIN_PROCESSOR, interceptorChainProcessor);
        setField(resourceObject, ACTION_MAPPER, actionMapper);
        setField(resourceObject, FILE_INPUT_DETAILS_FACTORY, fileInputDetailsFactory);
        setField(resourceObject, VALID_PARAMETER_COLLECTION_BUILDER_FACTORY_FIELD, validParameterCollectionBuilderFactory);
        setField(resourceObject, TRACE_LOGGER_FIELD, traceLogger);
        setField(resourceObject, HTTP_TRACE_LOGGER_HELPER_FIELD, httpTraceLoggerHelper);

        when(validParameterCollectionBuilderFactory.create()).thenReturn(new ValidParameterCollectionBuilder());

        return resourceObject;
    }
}
