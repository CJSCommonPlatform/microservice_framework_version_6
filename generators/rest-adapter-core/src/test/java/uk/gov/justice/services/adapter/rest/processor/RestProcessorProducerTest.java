package uk.gov.justice.services.adapter.rest.processor;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWithMemberAsFirstMethodOf;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.CustomAdapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestProcessorProducerTest {

    private static final String ID_VALUE = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME_VALUE = "test.command.do-something";
    private static final Collection<Parameter> NOT_USED_PATH_PARAMS = emptyList();
    private static final String FIELD_NAME = "name";
    private static final String FIELD_VALUE = "TEST NAME";

    private InjectionPoint queryApiInjectionPoint;
    private InjectionPoint queryControllerInjectionPoint;
    private InjectionPoint queryViewInjectionPoint;
    private InjectionPoint frameworkApiInjectionPoint;
    private InjectionPoint customApiInjectionPoint;

    @Mock
    private Function<JsonEnvelope, Optional<JsonEnvelope>> function;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Response response;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Spy
    private RestEnvelopeBuilderFactory envelopeBuilderFactory;

    @Mock
    private EnvelopeResponseFactory envelopeResponseFactory;

    @Mock
    private PayloadResponseFactory payloadResponseFactory;

    @InjectMocks
    private RestProcessorProducer restProcessorProducer;

    @Before
    public void setup() {
        queryApiInjectionPoint = injectionPointWithMemberAsFirstMethodOf(QueryApiAdapter.class);
        queryControllerInjectionPoint = injectionPointWithMemberAsFirstMethodOf(QueryControllerAdapter.class);
        queryViewInjectionPoint = injectionPointWithMemberAsFirstMethodOf(QueryViewAdapter.class);
        frameworkApiInjectionPoint = injectionPointWithMemberAsFirstMethodOf(FrameworkApiAdapter.class);
        customApiInjectionPoint = injectionPointWithMemberAsFirstMethodOf(CustomApiAdapter.class);

        restProcessorProducer.initialise();
    }

    @Test
    public void shouldReturnPayloadOnlyRestProcessorForQueryApi() {
        when(function.apply(any())).thenReturn(Optional.of(jsonEnvelope));
        when(payloadResponseFactory.responseFor("somecontext.somequery", Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessorProducer.produceRestProcessor(queryApiInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(result, sameInstance(response));
    }

    @Test
    public void shouldReturnPayloadOnlyRestProcessorForFrameworkApi() {
        when(function.apply(any())).thenReturn(Optional.of(jsonEnvelope));
        when(payloadResponseFactory.responseFor("somecontext.somequery", Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessorProducer.produceRestProcessor(frameworkApiInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(result, sameInstance(response));
    }

    @Test
    public void shouldReturnPayloadOnlyRestProcessorForCustomApi() {
        when(function.apply(any())).thenReturn(Optional.of(jsonEnvelope));
        when(payloadResponseFactory.responseFor("somecontext.somequery", Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessorProducer.produceRestProcessor(customApiInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(result, sameInstance(response));
    }

    @Test
    public void shouldReturnDefaultRestProcessorForQueryController() {
        when(function.apply(any())).thenReturn(Optional.of(jsonEnvelope));
        when(envelopeResponseFactory.responseFor("somecontext.somequery", Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessorProducer.produceRestProcessor(queryControllerInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(result, sameInstance(response));
    }

    @Test
    public void shouldReturnDefaultRestProcessorForQueryView() {
        when(function.apply(any())).thenReturn(Optional.of(jsonEnvelope));
        when(envelopeResponseFactory.responseFor("somecontext.somequery", Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessorProducer.produceRestProcessor(queryViewInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(result, sameInstance(response));
    }

    @Adapter(QUERY_API)
    private class QueryApiAdapter {

        public void test() {
        }
    }

    @Adapter(QUERY_CONTROLLER)
    private class QueryControllerAdapter {

        public void test() {
        }
    }

    @Adapter(QUERY_VIEW)
    private class QueryViewAdapter {

        public void test() {
        }
    }

    @FrameworkComponent("FRAMEWORK_API")
    private class FrameworkApiAdapter {

        public void test() {
        }
    }

    @CustomAdapter("CUSTOM_API")
    private class CustomApiAdapter {

        public void test() {
        }
    }
}