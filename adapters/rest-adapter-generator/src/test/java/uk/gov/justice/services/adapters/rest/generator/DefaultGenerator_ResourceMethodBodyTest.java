package uk.gov.justice.services.adapters.rest.generator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.messaging.DefaultEnvelope.envelopeFrom;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.messaging.Envelope;

@RunWith(MockitoJUnitRunner.class)
public class DefaultGenerator_ResourceMethodBodyTest {

    private static final JsonObject NOT_USED_JSONOBJECT = Json.createObjectBuilder().build();

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JavaCompilerUtil compiler;
    private DefaultGenerator generator;

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private RestProcessor restProcessorMock;

    @Before
    public void before() {
        generator = new DefaultGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/default/path")
                                .with(action(POST, "application/vnd.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE);
        Object resourceObject = instantiate(resourceClass);

        Response processorResponse = Response.ok().build();
        when(restProcessorMock.process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
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
                        resource("/default/path")
                                .with(action(POST, "application/vnd.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE);
        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(restProcessorMock).process(consumerCaptor.capture(), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class));

        Envelope envelope = envelopeFrom(null, null);
        consumerCaptor.getValue().accept(envelope);

        verify(dispatcher).dispatch(envelope);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassJsonObjectToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/default/path")
                                .with(action(POST, "application/vnd.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE);
        Object resourceObject = instantiate(resourceClass);

        JsonObject jsonObject = Json.createObjectBuilder().add("dummy", "abc").build();

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, jsonObject);

        verify(restProcessorMock).process(any(Consumer.class), eq(jsonObject), any(HttpHeaders.class), any(Map.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassHttpHeadersToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/default/path")
                                .with(action(POST, "application/vnd.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE);
        Object resourceObject = instantiate(resourceClass);

        HttpHeaders headers = new ResteasyHttpHeaders(new MultivaluedMapImpl<>());
        setField(resourceObject, "headers", headers);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessorMock).process(any(Consumer.class), any(JsonObject.class), eq(headers), any(Map.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(action(POST, "application/vnd.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE);

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessorMock).process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
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
                                .with(action(POST, "application/vnd.cmd-aa+json", "application/vnd.cmd-bb+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE);

        Object resourceObject = instantiate(resourceClass);

        List<Method> methods = methodsOf(resourceClass);

        Method secondMethod = methods.get(1);
        secondMethod.invoke(resourceObject, "paramValueXYZ", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessorMock).process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
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
                                .with(action(POST, "application/vnd.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE);

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessorMock).process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(2));
        assertThat(pathParams.containsKey("param1"), is(true));
        assertThat(pathParams.get("param1"), is("paramValueABC"));

        assertThat(pathParams.containsKey("param2"), is(true));
        assertThat(pathParams.get("param2"), is("paramValueDEF"));
    }

    private Object instantiate(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "restProcessor", restProcessorMock);
        setField(resourceObject, "dispatcher", dispatcher);
        return resourceObject;
    }

    private Method firstMethodOf(Class<?> resourceClass) {
        List<Method> methods = methodsOf(resourceClass);
        return methods.get(0);
    }

    private void setField(Object resourceObject, String fieldName, Object object)
            throws IllegalAccessException {
        Field field = fieldOf(resourceObject.getClass(), fieldName);
        field.setAccessible(true);
        field.set(resourceObject, object);
    }

    private Field fieldOf(Class<?> clazz, String fieldName) {
        Optional<Field> field = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getName().equals(fieldName))
                .findFirst();
        assertTrue(field.isPresent());
        return field.get();
    }

    private List<Method> methodsOf(Class<?> class1) {
        return Arrays.stream(class1.getDeclaredMethods()).filter(m -> !m.getName().contains("jacoco"))
                .collect(Collectors.toList());
    }

    private GeneratorConfig configurationWithBasePackage(String basePackageName) {
        Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName);
    }
}
