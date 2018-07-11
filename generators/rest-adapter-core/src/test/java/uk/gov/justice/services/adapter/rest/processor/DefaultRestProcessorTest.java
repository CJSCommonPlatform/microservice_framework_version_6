package uk.gov.justice.services.adapter.rest.processor;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.multipart.FileBasedInterceptorContextFactory;
import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;
import uk.gov.justice.services.adapter.rest.parameter.DefaultParameter;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.adapter.rest.parameter.ParameterType;
import uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategy;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRestProcessorTest {

    private static final String NOT_USED_ACTION = "actionABC";
    private static final String NOT_USED_RESPONSE_STRATEGY_NAME = "notUsedName";
    private static final List<Parameter> NOT_USED_PATH_PARAMS = emptyList();

    private static final Optional<JsonObject> NOT_USED_PAYLOAD = Optional.of(createObjectBuilder().build());
    private static final ResteasyHttpHeaders NOT_USED_HEADERS = headersWithUserId("123");

    @Mock
    private Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Response response;

    @Mock
    private ResponseStrategy responseStrategy;

    @Mock
    private ResponseStrategyCache responseStrategyCache;

    @Mock
    private FileBasedInterceptorContextFactory fileBasedInterceptorContextFactory;

    @Mock
    private Logger logger;

    @Mock
    private TraceLogger traceLogger;

    @Mock
    private HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Spy
    private RestEnvelopeBuilderFactory restEnvelopeBuilderFactory;

    @InjectMocks
    private DefaultRestProcessor restProcessor;


    @Test
    public void shouldPassEnvelopeWithPayloadToInterceptorChain() throws Exception {

        final String userId = "userId1234";
        final String action = "anAction123";
        final String payloadIdValue = "payloadIdValue1";
        final List<Parameter> pathParams = singletonList(DefaultParameter.valueOf("paramName", "someParamValue", ParameterType.STRING));

        final JsonObject payload = createObjectBuilder().add("payloadId", payloadIdValue).build();

        when(responseStrategyCache.responseStrategyOf(anyString())).thenReturn(responseStrategy);

        restProcessor.process(NOT_USED_RESPONSE_STRATEGY_NAME, interceptorChain, action, Optional.of(payload), headersWithUserId(userId), pathParams);

        final ArgumentCaptor<InterceptorContext> interceptorContextCaptor = forClass(InterceptorContext.class);
        verify(interceptorChain).apply(interceptorContextCaptor.capture());

        final InterceptorContext interceptorContext = interceptorContextCaptor.getValue();
        final JsonEnvelope envelope = interceptorContext.inputEnvelope();
        assertThat(envelope, jsonEnvelope()
                .withMetadataOf(metadata()
                        .withName(action)
                        .withUserId(userId))
                .withPayloadOf(payloadIsJson(allOf(
                        withJsonPath("$.payloadId", equalTo(payloadIdValue)),
                        withJsonPath("$.paramName", equalTo("someParamValue"))
                ))));
    }

    @Test
    public void shouldPassEnvelopeWithEmptyPayloadToInterceptorChain() throws Exception {
        final String action = "actionABC";
        final String userId = "usrABC";
        final List<Parameter> pathParams = singletonList(DefaultParameter.valueOf("name", "value123", ParameterType.STRING));

        when(responseStrategyCache.responseStrategyOf(anyString())).thenReturn(responseStrategy);

        restProcessor.process(NOT_USED_RESPONSE_STRATEGY_NAME, interceptorChain, action, headersWithUserId(userId), pathParams);

        final ArgumentCaptor<InterceptorContext> interceptorContextCaptor = forClass(InterceptorContext.class);
        verify(interceptorChain).apply(interceptorContextCaptor.capture());

        final InterceptorContext interceptorContext = interceptorContextCaptor.getValue();
        final JsonEnvelope envelope = interceptorContext.inputEnvelope();

        assertThat(envelope, jsonEnvelope()
                .withMetadataOf(metadata()
                        .withName(action)
                        .withUserId(userId))
                .withPayloadOf(payloadIsJson(
                        withJsonPath("$.name", equalTo("value123"))
                )));
    }

    @Test
    public void shouldReturnResponseFromResponseStrategyForCallWithPayload() throws Exception {

        when(interceptorChain.apply(any(InterceptorContext.class))).thenReturn(Optional.of(jsonEnvelope));
        final String action = "someActionABC";
        when(responseStrategy.responseFor(action, Optional.of(jsonEnvelope))).thenReturn(response);

        final String responseStrategyName = "someStrategy";

        when(responseStrategyCache.responseStrategyOf(responseStrategyName)).thenReturn(responseStrategy);
        final Response result = restProcessor.process(responseStrategyName, interceptorChain, action, NOT_USED_PAYLOAD, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        verify(responseStrategy).responseFor(action, Optional.of(jsonEnvelope));
        assertThat(result, equalTo(response));
    }

    @Test
    public void shouldReturnResponseFromResponseStrategyForCallWithoutPayload() throws Exception {
        when(interceptorChain.apply(any(InterceptorContext.class))).thenReturn(Optional.of(jsonEnvelope));

        final String action = "someActionBCD";
        when(responseStrategy.responseFor(action, Optional.of(jsonEnvelope))).thenReturn(response);

        final String responseStrategyName = "someOtherStrategy";
        when(responseStrategyCache.responseStrategyOf(responseStrategyName)).thenReturn(responseStrategy);

        final Response result = restProcessor.process(responseStrategyName, interceptorChain, action, NOT_USED_PAYLOAD, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        verify(responseStrategy).responseFor(action, Optional.of(jsonEnvelope));
        assertThat(result, equalTo(response));
    }

    @Test
    public void shouldCreateTheInterceptorContextUsingTheFileBasedInterceptorContextFactoryIfTheInputPartExists() throws Exception {
        final List<FileInputDetails> fileInputDetails = emptyList();
        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        when(fileBasedInterceptorContextFactory.create(eq(fileInputDetails), any(JsonEnvelope.class))).thenReturn(interceptorContext);
        when(responseStrategyCache.responseStrategyOf(anyString())).thenReturn(responseStrategy);

        restProcessor.process(NOT_USED_RESPONSE_STRATEGY_NAME, interceptorChain, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS, fileInputDetails);

        final ArgumentCaptor<InterceptorContext> interceptorContextCaptor = forClass(InterceptorContext.class);
        verify(interceptorChain).apply(interceptorContextCaptor.capture());

        final InterceptorContext resultInterceptorContext = interceptorContextCaptor.getValue();
        assertThat(resultInterceptorContext, equalTo(interceptorContext));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseFromResponseStrategyForCallWithInputPart() throws Exception {
        final List<FileInputDetails> fileInputDetails = mock(List.class);
        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        when(fileBasedInterceptorContextFactory.create(eq(fileInputDetails), any(JsonEnvelope.class))).thenReturn(interceptorContext);
        when(interceptorChain.apply(interceptorContext)).thenReturn(Optional.of(jsonEnvelope));

        final String responseStrategyName = "someOtherStrategy123";
        when(responseStrategyCache.responseStrategyOf(responseStrategyName)).thenReturn(responseStrategy);

        when(responseStrategy.responseFor(NOT_USED_ACTION, Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessor.process(responseStrategyName, interceptorChain, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS, fileInputDetails);

        verify(responseStrategy).responseFor(NOT_USED_ACTION, Optional.of(jsonEnvelope));
        assertThat(result, equalTo(response));
    }

    private static ResteasyHttpHeaders headersWithUserId(final String userId) {
        final ResteasyHttpHeaders headers;
        final MultivaluedMapImpl<String, String> requestHeaders = new MultivaluedMapImpl<>();
        requestHeaders.add(HeaderConstants.USER_ID, userId);
        headers = new ResteasyHttpHeaders(requestHeaders);
        return headers;
    }
}
