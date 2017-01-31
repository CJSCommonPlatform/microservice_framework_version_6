package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.builder.ResponseBuilder.response;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.adapter.rest.processor.ResponseStrategy;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.impl.tl.ThreadLocalHttpHeaders;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RestAdapterGenerator_POSTMethodBodyTest extends BaseRestAdapterGeneratorTest {

    private static final JsonObject NOT_USED_JSONOBJECT = Json.createObjectBuilder().build();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final Response processorResponse = Response.ok().build();
        when(restProcessor.process(any(ResponseStrategy.class), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class))).thenReturn(processorResponse);

        final Method method = firstMethodOf(resourceClass);

        final Object result = method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        assertThat(result, is(processorResponse));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldCallInterceptorChainProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Function> functionCaptor = ArgumentCaptor.forClass(Function.class);
        verify(restProcessor).process(any(ResponseStrategy.class), functionCaptor.capture(), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class));

        final JsonEnvelope envelope = envelope().build();
        functionCaptor.getValue().apply(envelope);

        verify(interceptorChainProcessor).process(envelope);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldProcessAsynchronouslyIfAcceptedResponseTypePresent() throws Exception {

        final Map<String, org.raml.model.Response> responses = new HashMap<>();
        responses.put(valueOf(INTERNAL_SERVER_ERROR.getStatusCode()), response().build());
        responses.put(valueOf(BAD_REQUEST.getStatusCode()), response().build());
        responses.put(valueOf(ACCEPTED.getStatusCode()), response().build());

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST)
                                        .withHttpActionOfDefaultRequestType()
                                        .withResponsesFrom(responses))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Function> functionCaptor = ArgumentCaptor.forClass(Function.class);
        verify(restProcessor).process(any(ResponseStrategy.class), functionCaptor.capture(), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class));

        final JsonEnvelope envelope = envelope().build();
        functionCaptor.getValue().apply(envelope);

        verify(interceptorChainProcessor).process(envelope);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassJsonObjectToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final Optional<JsonObject> jsonObject = Optional.of(Json.createObjectBuilder().add("dummy", "abc").build());

        final Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, jsonObject.get());

        verify(restProcessor).process(any(ResponseStrategy.class), any(Function.class), anyString(), eq(jsonObject), any(HttpHeaders.class), any(Collection.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassHttpHeadersToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final HttpHeaders headers = new ThreadLocalHttpHeaders();
        setField(resourceObject, "headers", headers);

        final Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).process(any(ResponseStrategy.class), any(Function.class), anyString(), any(Optional.class), eq(headers), any(Collection.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAResource");

        final Object resourceObject = getInstanceOf(resourceClass);

        final Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234", NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).process(any(ResponseStrategy.class), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        final Collection<Parameter> pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams, hasSize(1));
        final Parameter pathParam = pathParams.iterator().next();
        assertThat(pathParam.getName(), is("paramA"));
        assertThat(pathParam.getStringValue(), is("paramValue1234"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldInvoke2ndMethodAndPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{p1}", "p1")
                                .with(httpAction(POST,
                                        "application/vnd.type-aa+json",
                                        "application/vnd.type-bb+json")
                                        .with(mapping()
                                                .withName("cmd-aa")
                                                .withRequestType("application/vnd.type-aa+json"))
                                        .with(mapping()
                                                .withName("cmd-bb")
                                                .withRequestType("application/vnd.type-bb+json"))
                                )
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathP1Resource");

        final Object resourceObject = getInstanceOf(resourceClass);

        final List<Method> methods = methodsOf(resourceClass);

        final Method secondMethod = methods.get(1);
        secondMethod.invoke(resourceObject, "paramValueXYZ", NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).process(any(ResponseStrategy.class), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        final Collection<Parameter> pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams, hasSize(1));
        final Parameter pathParam = pathParams.iterator().next();
        assertThat(pathParam.getName(), is("p1"));
        assertThat(pathParam.getStringValue(), is("paramValueXYZ"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithTwoPathParamsToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{param1}/{param2}", "param1", "param2")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParam1Param2Resource");

        final Object resourceObject = getInstanceOf(resourceClass);

        final Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF", NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).process(any(ResponseStrategy.class), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        final Collection<Parameter> pathParams = pathParamsCaptor.getValue();

        assertThat(pathParams, hasSize(2));

        assertThat(pathParams, hasItems(
                allOf(hasProperty("name", equalTo("param1")), hasProperty("stringValue", equalTo("paramValueABC"))),
                allOf(hasProperty("name", equalTo("param2")), hasProperty("stringValue", equalTo("paramValueDEF")))
        ));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassActionToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/user")
                                .with(httpAction(POST)
                                        .with(mapping()
                                                .withName("contextA.someAction")
                                                .withRequestType("application/vnd.somemediatype1+json"))

                                        .withMediaType("application/vnd.somemediatype1+json", "json/schema/somemediatype1.json")
                                )
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUserResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final Class<?> actionMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultUserResourceActionMapper");
        final Object actionMapperObject = actionMapperClass.newInstance();
        setField(resourceObject, "actionMapper", actionMapperObject);

        setField(resourceObject, "headers", headersWith("Content-Type", "application/vnd.somemediatype1+json"));
        final Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).process(any(ResponseStrategy.class), any(Function.class), eq("contextA.someAction"), any(Optional.class), any(HttpHeaders.class), any(Collection.class));
    }

}
