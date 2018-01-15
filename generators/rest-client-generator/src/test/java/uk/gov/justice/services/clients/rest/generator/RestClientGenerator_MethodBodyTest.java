package uk.gov.justice.services.clients.rest.generator;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static org.raml.model.ParamType.BOOLEAN;
import static org.raml.model.ParamType.INTEGER;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.QueryParamBuilder.queryParam;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultGetResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.adapter.rest.parameter.ParameterType;
import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestClientGenerator_MethodBodyTest extends BaseGeneratorTest {

    private static final JsonEnvelope NOT_USED_ENVELOPE = mock(JsonEnvelope.class);

    @Mock
    private RestClientProcessor restClientProcessor;

    @Mock
    private RestClientHelper restClientHelper;

    @Mock
    private Enveloper enveloper;

    @Before
    public void before() {
        super.before();
        generator = new RestClientGenerator();
    }

    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingBaseUri() throws Exception {

        generator.run(
                restRamlWithDefaults()
                        .withBaseUri("http://localhost:8080/contextabc/query/api/rest/service1")
                        .withDefaultGetResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2Service1QueryApi");
        invokeFirstMethod(clazz);

        assertThat(capturedGetEndpointDefinition().getBaseUri(), is("http://localhost:8080/contextabc/query/api/rest/service1"));
    }

    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingPath() throws Exception {

        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceQueryApi");
        invokeFirstMethod(clazz);
        assertThat(capturedGetEndpointDefinition().getPath(), is("/pathabc/{anId}"));
    }


    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingPathParams() throws Exception {

        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/pathabc").with(httpAction().withHttpActionType(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceQueryApi");

        final ImmutableSet<String> paramsSetReturnedByHelper = ImmutableSet.of("aaa");
        when(restClientHelper.extractPathParametersFromPath("/pathabc")).thenReturn(paramsSetReturnedByHelper);

        invokeFirstMethod(clazz);

        assertThat(capturedGetEndpointDefinition().getPathParams(), is(paramsSetReturnedByHelper));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallRestClientWithEndpointDefinitionContainingQueryParams() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(defaultGetResource()
                                .with(defaultGetAction()
                                        .with(
                                                queryParam("qparam1").required(true),
                                                queryParam("qparam2").required(false),
                                                queryParam("qparam3").required(false).withType(INTEGER),
                                                queryParam("qparam4").required(false).withType(BOOLEAN))
                                ))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceQueryApi");

        invokeFirstMethod(clazz);

        final EndpointDefinition endpointDefinition = capturedGetEndpointDefinition();

        assertThat(endpointDefinition.getQueryParams(), hasSize(4));
        assertThat(endpointDefinition.getQueryParams(), hasItems(
                allOf(hasProperty("name", is("qparam1")), hasProperty("required", is(true)), hasProperty("type", is(ParameterType.STRING))),
                allOf(hasProperty("name", is("qparam2")), hasProperty("required", is(false)), hasProperty("type", is(ParameterType.STRING))),
                allOf(hasProperty("name", is("qparam3")), hasProperty("required", is(false)), hasProperty("type", is(ParameterType.NUMERIC))),
                allOf(hasProperty("name", is("qparam4")), hasProperty("required", is(false)), hasProperty("type", is(ParameterType.BOOLEAN)))
        ));
    }

    @Test
    public void shouldPassEnvelopeToRestClient() throws Exception {

        generator.run(
                restRamlWithQueryApiDefaults()
                        .withDefaultGetResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceQueryApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).get(any(EndpointDefinition.class), same(envelope));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSetNameAndPassEnvelopeToRestClient() throws Exception {

        generateRemoteSomeComponent2ServiceCommandApi();

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).post(any(EndpointDefinition.class), eq(outputEnvelope));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallRestClientWithEndpointDefinitionContainingMediaType() throws Exception {

        generateRemoteSomeComponent2ServiceCommandApi();

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        assertThat(capturedPostEndpointDefinition().getResponseMediaType(), is("ctx.defcmd"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallSynchronousPostRestClientMethod() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(POST)
                                .withMediaTypeWithDefaultSchema("application/vnd.ctx.defcmd+json")
                                .withResponseTypes("application/vnd.ctx.response+json")
                                .with(mapping()
                                        .withName("action1")
                                        .withRequestType("application/vnd.ctx.defcmd+json")
                                        .withResponseType("application/vnd.ctx.response+json"))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).synchronousPost(any(EndpointDefinition.class), eq(outputEnvelope));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallAsynchronousPutRestClientMethod() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(PUT)
                                .withMediaTypeWithDefaultSchema("application/vnd.ctx.defcmd+json")
                                .with(mapping()
                                        .withName("action1")
                                        .withRequestType("application/vnd.ctx.defcmd+json"))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).put(any(EndpointDefinition.class), eq(outputEnvelope));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallSynchronousPutRestClientMethod() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(PUT)
                                .withMediaTypeWithDefaultSchema("application/vnd.ctx.defcmd+json")
                                .withResponseTypes("application/vnd.ctx.response+json")
                                .with(mapping()
                                        .withName("action1")
                                        .withRequestType("application/vnd.ctx.defcmd+json")
                                        .withResponseType("application/vnd.ctx.response+json"))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).synchronousPut(any(EndpointDefinition.class), eq(outputEnvelope));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallAsynchronousPatchRestClientMethod() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(PATCH)
                                .withMediaTypeWithDefaultSchema("application/vnd.ctx.defcmd+json")
                                .with(mapping()
                                        .withName("action1")
                                        .withRequestType("application/vnd.ctx.defcmd+json"))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).patch(any(EndpointDefinition.class), eq(outputEnvelope));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallSynchronousPatchRestClientMethod() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(PATCH)
                                .withMediaTypeWithDefaultSchema("application/vnd.ctx.defcmd+json")
                                .withResponseTypes("application/vnd.ctx.response+json")
                                .with(mapping()
                                        .withName("action1")
                                        .withRequestType("application/vnd.ctx.defcmd+json")
                                        .withResponseType("application/vnd.ctx.response+json"))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);

        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).synchronousPatch(any(EndpointDefinition.class), eq(outputEnvelope));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallAsynchronousDELETERestClientMethod() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(DELETE)
                                .withMediaTypeWithDefaultSchema("application/vnd.ctx.defcmd+json")
                                .with(mapping()
                                        .withName("action1")
                                        .withRequestType("application/vnd.ctx.defcmd+json"))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final Function function = mock(Function.class);

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteSomeComponent2ServiceCommandApi");
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();

        when(enveloper.withMetadataFrom(envelope, "ctx.defcmd")).thenReturn(function);
        when(function.apply(envelope.payload())).thenReturn(outputEnvelope);

        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).delete(any(EndpointDefinition.class), eq(outputEnvelope));
    }

    private Object instanceOfRemoteClient(final Class<?> clazz) throws InstantiationException, IllegalAccessException {
        final Object remoteClient = instanceOf(clazz);
        setField(remoteClient, "traceLogger", new DefaultTraceLogger());
        return remoteClient;
    }

    private void invokeFirstMethod(final Class<?> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        final Object remoteClient = instanceOfRemoteClient(clazz);
        final Method method = firstMethodOf(clazz).get();
        method.invoke(remoteClient, NOT_USED_ENVELOPE);
    }

    private EndpointDefinition capturedGetEndpointDefinition() {
        final ArgumentCaptor<EndpointDefinition> endpointDefCaptor = ArgumentCaptor.forClass(EndpointDefinition.class);

        verify(restClientProcessor).get(endpointDefCaptor.capture(), any(JsonEnvelope.class));
        return endpointDefCaptor.getValue();
    }

    private EndpointDefinition capturedPostEndpointDefinition() {
        final ArgumentCaptor<EndpointDefinition> endpointDefCaptor = ArgumentCaptor.forClass(EndpointDefinition.class);

        verify(restClientProcessor).post(endpointDefCaptor.capture(), any(JsonEnvelope.class));
        return endpointDefCaptor.getValue();
    }

    private Object instanceOf(final Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "restClientProcessor", restClientProcessor);
        setField(resourceObject, "restClientHelper", restClientHelper);
        setField(resourceObject, "enveloper", enveloper);
        return resourceObject;
    }

    private void generateRemoteSomeComponent2ServiceCommandApi() {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(POST)
                                .withMediaTypeWithDefaultSchema("application/vnd.ctx.defcmd+json")
                                .with(mapping()
                                        .withName("action1")
                                        .withRequestType("application/vnd.ctx.defcmd+json"))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));
    }

}
