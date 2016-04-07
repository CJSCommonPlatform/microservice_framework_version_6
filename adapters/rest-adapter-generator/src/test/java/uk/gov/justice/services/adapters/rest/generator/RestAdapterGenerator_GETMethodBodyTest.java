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
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcher;
import uk.gov.justice.services.messaging.Envelope;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.messaging.DefaultEnvelope.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class RestAdapterGenerator_GETMethodBodyTest {

    private static final JsonObject NOT_USED_JSONOBJECT = Json.createObjectBuilder().build();

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JavaCompilerUtil compiler;
    private RestAdapterGenerator generator;

    @Mock
    private SynchronousDispatcher dispatcher;

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
                                .with(action(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        Response processorResponse = Response.ok().build();
        when(restProcessor.processSynchronously(any(Function.class), any(HttpHeaders.class), any(Map.class))).thenReturn(processorResponse);

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
                                .with(action(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject);

        ArgumentCaptor<Function> consumerCaptor = ArgumentCaptor.forClass(Function.class);
        verify(restProcessor).processSynchronously(consumerCaptor.capture(), any(HttpHeaders.class), any(Map.class));

        Envelope envelope = envelopeFrom(null, null);
        consumerCaptor.getValue().apply(envelope);

        verify(dispatcher).dispatch(envelope);

    }


    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassHttpHeadersToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(action(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        HttpHeaders headers = new ThreadLocalHttpHeaders();

        setField(resourceObject, "headers", headers);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject);

        verify(restProcessor).processSynchronously(any(Function.class), eq(headers), any(Map.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(action(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAResource");

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234");

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), any(HttpHeaders.class),
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
                                .with(action(GET).withDefaultResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParam1Param2Resource");

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF");

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processSynchronously(any(Function.class), any(HttpHeaders.class),
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
                        .with(resource("/pathA").with(action(GET).withDefaultResponseType()))
                        .with(resource("/pathB").with(action(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE));

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
        setField(resourceObject, "syncDispatcher", dispatcher);
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
