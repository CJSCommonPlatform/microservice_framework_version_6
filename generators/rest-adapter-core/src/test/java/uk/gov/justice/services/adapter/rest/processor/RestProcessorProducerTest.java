package uk.gov.justice.services.adapter.rest.processor;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWithMemberAsFirstMethodOf;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Mock
    private Function<JsonEnvelope, Optional<JsonEnvelope>> function;

    @InjectMocks
    private RestProcessorProducer restProcessorProducer;

    @Before
    public void setup() {
        queryApiInjectionPoint = injectionPointWithMemberAsFirstMethodOf(QueryApiAdapter.class);
        queryControllerInjectionPoint = injectionPointWithMemberAsFirstMethodOf(QueryControllerAdapter.class);
        queryViewInjectionPoint = injectionPointWithMemberAsFirstMethodOf(QueryViewAdapter.class);
        frameworkApiInjectionPoint = injectionPointWithMemberAsFirstMethodOf(FrameworkApiAdapter.class);

        restProcessorProducer.envelopeBuilderFactory = new RestEnvelopeBuilderFactory();
        restProcessorProducer.jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        restProcessorProducer.initialise();
    }

    @Test
    public void shouldReturnPayloadOnlyRestProcessorForQueryApi() {
        when(function.apply(any())).thenReturn(
                Optional.of(envelope().with(metadataOf(UUID.fromString(ID_VALUE), NAME_VALUE)).withPayloadOf(FIELD_VALUE, FIELD_NAME).build()));

        final Response response = restProcessorProducer.produceRestProcessor(queryApiInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(response, notNullValue());
        assertThat(response.getHeaderString(HeaderConstants.ID), equalTo(ID_VALUE));
        with(response.getEntity().toString())
                .assertNotDefined(METADATA)
                .assertThat("$." + FIELD_NAME, equalTo(FIELD_VALUE));
    }

    @Test
    public void shouldReturnPayloadOnlyRestProcessorForFrameworkApi() {
        when(function.apply(any())).thenReturn(
                Optional.of(envelope().with(metadataOf(UUID.fromString(ID_VALUE), NAME_VALUE)).withPayloadOf(FIELD_VALUE, FIELD_NAME).build()));

        final Response response = restProcessorProducer.produceRestProcessor(frameworkApiInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(response, notNullValue());
        assertThat(response.getHeaderString(HeaderConstants.ID), equalTo(ID_VALUE));
        with(response.getEntity().toString())
                .assertNotDefined(METADATA)
                .assertThat("$." + FIELD_NAME, equalTo(FIELD_VALUE));
    }

    @Test
    public void shouldReturnDefaultRestProcessorForQueryController() {
        when(function.apply(any())).thenReturn(
                Optional.of(envelope().with(metadataOf(UUID.fromString(ID_VALUE), NAME_VALUE)).withPayloadOf(FIELD_VALUE, FIELD_NAME).build()));

        final Response response = restProcessorProducer.produceRestProcessor(queryControllerInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(response, notNullValue());
        with(response.getEntity().toString())
                .assertThat("$." + FIELD_NAME, equalTo(FIELD_VALUE))
                .assertThat("$._metadata.id", equalTo(ID_VALUE))
                .assertThat("$._metadata.name", equalTo(NAME_VALUE));
    }

    @Test
    public void shouldReturnDefaultRestProcessorForQueryView() {
        when(function.apply(any())).thenReturn(
                Optional.of(envelope().with(metadataOf(UUID.fromString(ID_VALUE), NAME_VALUE)).withPayloadOf(FIELD_VALUE, FIELD_NAME).build()));

        final Response response = restProcessorProducer.produceRestProcessor(queryViewInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(response, notNullValue());
        with(response.getEntity().toString())
                .assertThat("$." + FIELD_NAME, equalTo(FIELD_VALUE))
                .assertThat("$._metadata.id", equalTo(ID_VALUE))
                .assertThat("$._metadata.name", equalTo(NAME_VALUE));
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
}