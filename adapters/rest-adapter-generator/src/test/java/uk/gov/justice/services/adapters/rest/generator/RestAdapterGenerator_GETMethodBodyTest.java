package uk.gov.justice.services.adapters.rest.generator;


import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.adapters.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.adapters.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;

import uk.gov.justice.services.adapter.rest.BasicActionMapper;
import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcher;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.impl.tl.ThreadLocalHttpHeaders;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class RestAdapterGenerator_GETMethodBodyTest extends BaseRestAdapterGeneratorTest {

    private static final String NULL_STRING_VALUE = null;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private SynchronousDispatcher dispatcher;
    @Mock
    private BasicActionMapper actionMapper;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instanceOf(resourceClass);

        Response processorResponse = Response.ok().build();
        when(restProcessor.processSynchronously(any(Function.class), anyString(), any(HttpHeaders.class), any(Map.class))).thenReturn(processorResponse);

        Method method = firstMethodOf(resourceClass);

        Object result = method.invoke(resourceObject);

        assertThat(result, is(processorResponse));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldCallDispatcher() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject);

        ArgumentCaptor<Function> consumerCaptor = ArgumentCaptor.forClass(Function.class);
        verify(restProcessor).processSynchronously(consumerCaptor.capture(), anyString(), any(HttpHeaders.class), any(Map.class));

        JsonEnvelope envelope = envelopeFrom(null, null);
        consumerCaptor.getValue().apply(envelope);

        verify(dispatcher).dispatch(envelope);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassHttpHeadersToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instanceOf(resourceClass);

        HttpHeaders headers = new ThreadLocalHttpHeaders();

        setField(resourceObject, "headers", headers);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject);

        verify(restProcessor).processSynchronously(any(Function.class), anyString(), eq(headers), any(Map.class));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassActionToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/user")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("contextA.action1")
                                                .withResponseType("application/vnd.ctx.query.somemediatype1+json"))
                                        .with(mapping()
                                                .withName("contextA.action1")
                                                .withResponseType("application/vnd.ctx.query.somemediatype2+json"))
                                        .with(mapping()
                                                .withName("contextA.action2")
                                                .withResponseType("application/vnd.ctx.query.somemediatype3+json"))
                                        .withResponseTypes(
                                                "application/vnd.ctx.query.somemediatype1+json",
                                                "application/vnd.ctx.query.somemediatype2+json",
                                                "application/vnd.ctx.query.somemediatype3+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ACTION_MAPPING_TRUE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUserResource");
        Object resourceObject = instanceOf(resourceClass);

        Class<?> actionMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultUserResourceActionMapper");
        Object actionMapperObject = actionMapperClass.newInstance();
        setField(resourceObject, "actionMapper", actionMapperObject);

        setField(resourceObject, "headers", headersWith("Accept", "application/vnd.ctx.query.somemediatype1+json"));

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject);


        verify(restProcessor).processSynchronously(any(Function.class), eq("contextA.action1"),
                any(HttpHeaders.class), any(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassActionToRestProcessor2() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/case")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("contextB.action1")
                                                .withResponseType("application/vnd.ctx.query.mediatype1+json"))
                                        .withResponseTypes("application/vnd.ctx.query.mediatype1+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ACTION_MAPPING_TRUE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultCaseResource");
        Object resourceObject = instanceOf(resourceClass);


        Class<?> actionMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        Object actionMapperObject = actionMapperClass.newInstance();
        setField(resourceObject, "actionMapper", actionMapperObject);

        setField(resourceObject, "headers", headersWith("Accept", "application/vnd.ctx.query.mediatype1+json"));

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject);


        verify(restProcessor).processSynchronously(any(Function.class), eq("contextB.action1"),
                any(HttpHeaders.class), any(Map.class));
    }



    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(httpAction(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAResource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234");

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), anyString(), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(1));
        assertThat(pathParams.containsKey("paramA"), is(true));
        assertThat(pathParams.get("paramA"), is("paramValue1234"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithTwoPathParamsToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{param1}/{param2}", "param1", "param2")
                                .with(httpAction(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParam1Param2Resource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF");

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), anyString(), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(2));
        assertThat(pathParams.containsKey("param1"), is(true));
        assertThat(pathParams.get("param1"), is("paramValueABC"));

        assertThat(pathParams.containsKey("param2"), is(true));
        assertThat(pathParams.get("param2"), is("paramValueDEF"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOneQueryParamToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(httpAction(GET)
                                        .withQueryParameters("queryParam")
                                        .withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234");

        ArgumentCaptor<Map> queryParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), anyString(), any(HttpHeaders.class),
                queryParamsCaptor.capture());

        Map queryParams = queryParamsCaptor.getValue();
        assertThat(queryParams.entrySet().size(), is(1));
        assertThat(queryParams.containsKey("queryParam"), is(true));
        assertThat(queryParams.get("queryParam"), is("paramValue1234"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithTwoQueryParamsToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(httpAction(GET)
                                        .withQueryParameters("queryParam1", "queryParam2")
                                        .withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);

        boolean queryParam1IsFirstMethodParameter = method.getParameters()[0].getName().equals("queryParam1");
        if (queryParam1IsFirstMethodParameter) {
            method.invoke(resourceObject, "paramValueABC", "paramValueDEF");
        } else {
            method.invoke(resourceObject, "paramValueDEF", "paramValueABC");
        }

        ArgumentCaptor<Map> queryParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), anyString(), any(HttpHeaders.class),
                queryParamsCaptor.capture());

        Map queryParams = queryParamsCaptor.getValue();
        assertThat(queryParams.entrySet().size(), is(2));
        assertThat(queryParams.containsKey("queryParam1"), is(true));
        assertThat(queryParams.get("queryParam1"), is("paramValueABC"));

        assertThat(queryParams.containsKey("queryParam2"), is(true));
        assertThat(queryParams.get("queryParam2"), is("paramValueDEF"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamAndOneQueryParamToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{param}", "param")
                                .with(httpAction(GET)
                                        .withQueryParameters("queryParam")
                                        .withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamResource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF");

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), anyString(), any(HttpHeaders.class),
                paramsCaptor.capture());

        Map params = paramsCaptor.getValue();
        assertThat(params.entrySet().size(), is(2));
        assertThat(params.containsKey("param"), is(true));
        assertThat(params.get("param"), is("paramValueABC"));

        assertThat(params.containsKey("queryParam"), is(true));
        assertThat(params.get("queryParam"), is("paramValueDEF"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldRemoveOptionalQueryParamIfSetToNull() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(httpAction(GET)
                                        .withQueryParameters("queryParam1")
                                        .withOptionalQueryParameters("queryParam2")
                                        .withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);

        boolean queryParam1IsFirstMethodParameter = method.getParameters()[0].getName().equals("queryParam1");
        if (queryParam1IsFirstMethodParameter) {
            method.invoke(resourceObject, "paramValueABC", NULL_STRING_VALUE);
        } else {
            method.invoke(resourceObject, NULL_STRING_VALUE, "paramValueABC");
        }

        ArgumentCaptor<Map> queryParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), anyString(), any(HttpHeaders.class),
                queryParamsCaptor.capture());

        Map queryParams = queryParamsCaptor.getValue();
        assertThat(queryParams.entrySet().size(), is(1));
        assertThat(queryParams.containsKey("queryParam1"), is(true));
        assertThat(queryParams.get("queryParam1"), is("paramValueABC"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldThrowExceptionIfRequiredQueryParamIsNull() throws Exception {
        exception.expect(InvocationTargetException.class);
        exception.expectCause(isA(BadRequestException.class));

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(httpAction(GET)
                                        .withQueryParameters("queryParam1")
                                        .withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Object resourceObject = instanceOf(resourceClass);

        Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject, NULL_STRING_VALUE);
    }

    private Object instanceOf(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "restProcessor", restProcessor);
        setField(resourceObject, "syncDispatcher", dispatcher);
        setField(resourceObject, "actionMapper", actionMapper);
        return resourceObject;
    }


}
