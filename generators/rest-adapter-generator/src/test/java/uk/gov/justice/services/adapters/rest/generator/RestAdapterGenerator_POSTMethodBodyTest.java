package uk.gov.justice.services.adapters.rest.generator;

import static java.util.Collections.emptyMap;
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
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.adapter.rest.BasicActionMapper;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.impl.tl.ThreadLocalHttpHeaders;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class RestAdapterGenerator_POSTMethodBodyTest extends BaseRestAdapterGeneratorTest {

    private static final JsonObject NOT_USED_JSONOBJECT = Json.createObjectBuilder().build();
    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";
    private static final String REST_PROCESSOR = "restProcessor";
    private static final String ACTION_MAPPER = "actionMapper";

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    private BasicActionMapper actionMapper;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instanceOf(resourceClass);

        Response processorResponse = Response.ok().build();
        when(restProcessor.processAsynchronously(any(Consumer.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class))).thenReturn(processorResponse);

        Method method = firstMethodOf(resourceClass);

        Object result = method.invoke(resourceObject, NOT_USED_JSONOBJECT);

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

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(restProcessor).processAsynchronously(consumerCaptor.capture(), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class));

        JsonEnvelope envelope = envelope().build();
        consumerCaptor.getValue().accept(envelope);

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

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instanceOf(resourceClass);

        Optional<JsonObject> jsonObject = Optional.of(Json.createObjectBuilder().add("dummy", "abc").build());

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, jsonObject.get());

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), eq(jsonObject), any(HttpHeaders.class), any(Collection.class));
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

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instanceOf(resourceClass);

        HttpHeaders headers = new ThreadLocalHttpHeaders();
        setField(resourceObject, "headers", headers);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(Optional.class), eq(headers), any(Collection.class));
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

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAResource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Collection<Parameter> pathParams = pathParamsCaptor.getValue();
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

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathP1Resource");

        Object resourceObject = instanceOf(resourceClass);

        List<Method> methods = methodsOf(resourceClass);

        Method secondMethod = methods.get(1);
        secondMethod.invoke(resourceObject, "paramValueXYZ", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Collection<Parameter> pathParams = pathParamsCaptor.getValue();
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

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParam1Param2Resource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Collection<Parameter> pathParams = pathParamsCaptor.getValue();

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

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUserResource");
        Object resourceObject = instanceOf(resourceClass);

        Class<?> actionMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultUserResourceActionMapper");
        Object actionMapperObject = actionMapperClass.newInstance();
        setField(resourceObject, "actionMapper", actionMapperObject);

        setField(resourceObject, "headers", headersWith("Content-Type", "application/vnd.somemediatype1+json"));
        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).processAsynchronously(any(Consumer.class), eq("contextA.someAction"), any(Optional.class), any(HttpHeaders.class), any(Collection.class));
    }

    private Object instanceOf(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, REST_PROCESSOR, restProcessor);
        setField(resourceObject, INTERCEPTOR_CHAIN_PROCESSOR, interceptorChainProcessor);
        setField(resourceObject, ACTION_MAPPER, actionMapper);
        return resourceObject;
    }


}
