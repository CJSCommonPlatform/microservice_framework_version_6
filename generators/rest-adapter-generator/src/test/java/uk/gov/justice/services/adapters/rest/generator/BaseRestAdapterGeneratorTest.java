package uk.gov.justice.services.adapters.rest.generator;


import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.adapter.rest.BasicActionMapper;
import uk.gov.justice.services.adapter.rest.mutipart.FileInputDetailsFactory;
import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.adapter.rest.processor.response.AcceptedStatusNoEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopeEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopePayloadEntityResponseStrategy;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseRestAdapterGeneratorTest extends BaseGeneratorTest {

    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";
    private static final String REST_PROCESSOR = "restProcessor";
    private static final String ACTION_MAPPER = "actionMapper";
    private static final String RESPONSE_STRATEGY_FIELD = "responseStrategy";
    private static final String ACCEPTED_STATUS_NO_ENTITY_RESPONSE_STRATEGY_FIELD = "acceptedStatusNoEntityResponseStrategy";
    private static final String FILE_INPUT_DETAILS_FACTORY = "fileInputDetailsFactory";

    @Mock
    protected InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    protected BasicActionMapper actionMapper;

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

    @Before
    public void before() {
        super.before();
        generator = new RestAdapterGenerator();
    }

    protected Object getInstanceOf(final Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, REST_PROCESSOR, restProcessor);
        setField(resourceObject, INTERCEPTOR_CHAIN_PROCESSOR, interceptorChainProcessor);
        setField(resourceObject, ACTION_MAPPER, actionMapper);
        setField(resourceObject, RESPONSE_STRATEGY_FIELD, okStatusEnvelopeEntityResponseStrategy);
        setField(resourceObject, ACCEPTED_STATUS_NO_ENTITY_RESPONSE_STRATEGY_FIELD, acceptedStatusNoEntityResponseStrategy);
        setField(resourceObject, FILE_INPUT_DETAILS_FACTORY, fileInputDetailsFactory);
        return resourceObject;
    }
}
