package uk.gov.justice.services.adapters.rest.generator;


import org.apache.cxf.jaxrs.impl.tl.ThreadLocalHttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class RestAdapterGenerator_POSTMethodBodyTest {

    private static final JsonObject NOT_USED_JSONOBJECT = Json.createObjectBuilder().build();

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JavaCompilerUtil compiler;
    private RestAdapterGenerator generator;

    @Mock
    private AsynchronousDispatcher dispatcher;

    @Mock
    private RestProcessor restProcessor;

    @Before
    public void before() {
        generator = new RestAdapterGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(action(POST).withDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        Response processorResponse = Response.ok().build();
        when(restProcessor.processAsynchronously(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class))).thenReturn(processorResponse);

        Method method = firstMethodOf(resourceClass);

        Object result = method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        assertThat(result, is(processorResponse));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldCallDispatcher() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(action(POST).withDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(restProcessor).processAsynchronously(consumerCaptor.capture(), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class));

        JsonEnvelope jsonEnvelope = envelopeFrom(null, null);
        consumerCaptor.getValue().accept(jsonEnvelope);

        verify(dispatcher).dispatch(jsonEnvelope);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassJsonObjectToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(action(POST).withDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        JsonObject jsonObject = Json.createObjectBuilder().add("dummy", "abc").build();

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, jsonObject);

        verify(restProcessor).processAsynchronously(any(Consumer.class), eq(jsonObject), any(HttpHeaders.class), any(Map.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassHttpHeadersToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(action(POST).withDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        HttpHeaders headers = new ThreadLocalHttpHeaders();
        setField(resourceObject, "headers", headers);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).processAsynchronously(any(Consumer.class), any(JsonObject.class), eq(headers), any(Map.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(action(POST).withDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAResource");

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(1));
        assertThat(pathParams.containsKey("paramA"), is(true));
        assertThat(pathParams.get("paramA"), is("paramValue1234"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessorWhenInvoking2ndMethod() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{p1}", "p1")
                                .with(action(POST, "application/vnd.ctx.command.cmd-aa+json", "application/vnd.ctx.command.cmd-bb+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathP1Resource");

        Object resourceObject = instantiate(resourceClass);

        List<Method> methods = methodsOf(resourceClass);

        Method secondMethod = methods.get(1);
        secondMethod.invoke(resourceObject, "paramValueXYZ", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(1));
        assertThat(pathParams.containsKey("p1"), is(true));
        assertThat(pathParams.get("p1"), is("paramValueXYZ"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithTwoPathParamsToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{param1}/{param2}", "param1", "param2")
                                .with(action(POST).withDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParam1Param2Resource");

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(2));
        assertThat(pathParams.containsKey("param1"), is(true));
        assertThat(pathParams.get("param1"), is("paramValueABC"));

        assertThat(pathParams.containsKey("param2"), is(true));
        assertThat(pathParams.get("param2"), is("paramValueDEF"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnSetOfResourceClasses() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/pathA").with(action(POST).withDefaultRequestType()))
                        .with(resource("/pathB").with(action(POST).withDefaultRequestType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Set<Class<?>> compiledClasses = compiler.compiledClassesOf(BASE_PACKAGE);
        Class<?> applicationClass = compiler.classOf(compiledClasses, BASE_PACKAGE, "CommandApiRestServiceApplication");
        Object application = applicationClass.newInstance();

        Method method = applicationClass.getDeclaredMethod("getClasses");
        Object result = method.invoke(application);
        assertThat(result, is(instanceOf(Set.class)));
        Set<Class<?>> classes = (Set<Class<?>>) result;
        assertThat(classes, hasSize(2));
        assertThat(classes, containsInAnyOrder(
                equalTo(compiler.classOf(compiledClasses, BASE_PACKAGE, "resource", "DefaultPathAResource")),
                equalTo(compiler.classOf(compiledClasses, BASE_PACKAGE, "resource", "DefaultPathBResource"))));
    }

    private Object instantiate(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "restProcessor", restProcessor);
        setField(resourceObject, "asyncDispatcher", dispatcher);
        return resourceObject;
    }


}
