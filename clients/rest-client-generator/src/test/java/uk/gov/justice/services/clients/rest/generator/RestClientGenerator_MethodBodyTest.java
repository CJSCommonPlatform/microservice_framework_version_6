package uk.gov.justice.services.clients.rest.generator;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.defaultGetAction;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.defaultGetResource;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder;
import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;
import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.ParamType;
import org.raml.model.parameter.QueryParameter;

@RunWith(MockitoJUnitRunner.class)
public class RestClientGenerator_MethodBodyTest {
    private static final String BASE_PACKAGE = "org.raml.test";
    private static final JsonEnvelope NOT_USED_ENVELOPE = DefaultJsonEnvelope.envelopeFrom(null, null);
    private static final Map<String, String> NOT_USED_GENERATOR_PROPERTIES = ImmutableMap.of("serviceComponent", "QUERY_CONTROLLER");
    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    @Mock
    RestClientProcessor restClientProcessor;
    @Mock
    RestClientHelper restClientHelper;
    private RestClientGenerator restClientGenerator;
    private JavaCompilerUtil compiler;

    @Before
    public void before() {

        restClientGenerator = new RestClientGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingBaseUri() throws Exception {

        restClientGenerator.run(
                restRamlWithDefaults()
                        .withBaseUri("http://localhost:8080/contextabc/query/api/rest/service1")
                        .withDefaultGetResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteService1QueryApi");
        invokeFirstMethod(clazz);

        assertThat(capturedEndpointDefinition().getBaseURi(), is("http://localhost:8080/contextabc/query/api/rest/service1"));
    }

    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingPath() throws Exception {

        restClientGenerator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/pathabc/{anId}").with(ActionBuilder.action().withActionType(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");
        invokeFirstMethod(clazz);
        assertThat(capturedEndpointDefinition().getPath(), is("/pathabc/{anId}"));
    }


    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingPathParams() throws Exception {

        restClientGenerator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/pathabc").with(ActionBuilder.action().withActionType(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");

        ImmutableSet<String> paramsSetReturnedByHelper = ImmutableSet.of("aaa");
        when(restClientHelper.extractPathParametersFromPath("/pathabc")).thenReturn(paramsSetReturnedByHelper);

        invokeFirstMethod(clazz);

        assertThat(capturedEndpointDefinition().getPathParams(), is(paramsSetReturnedByHelper));
    }

    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingQueryParams() throws Exception {
        restClientGenerator.run(
                restRamlWithQueryApiDefaults()
                        .with(defaultGetResource()
                                .with(defaultGetAction()
                                        .withQueryParameters(queryParameterOf("qparam1", true), queryParameterOf("qparam2", false))))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");

        invokeFirstMethod(clazz);

        EndpointDefinition endpointDefinition = capturedEndpointDefinition();

        assertThat(endpointDefinition.getQueryParams(), hasSize(2));
        assertThat(endpointDefinition.getQueryParams(), hasItems(
                allOf(hasProperty("name", is("qparam1")), hasProperty("required", is(true))),
                allOf(hasProperty("name", is("qparam2")), hasProperty("required", is(false)))
        ));
    }

    @Test
    public void shouldPassEnvelopeToRestClient() throws Exception {

        restClientGenerator.run(
                restRamlWithQueryApiDefaults()
                        .withDefaultGetResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");
        Object remoteClient = instanceOf(clazz);
        Method method = firstMethodOf(clazz);

        JsonEnvelope envelope = DefaultJsonEnvelope.envelopeFrom(null, null);
        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).request(any(EndpointDefinition.class), same(envelope));
    }

    private void invokeFirstMethod(final Class<?> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object remoteClient = instanceOf(clazz);
        Method method = firstMethodOf(clazz);
        method.invoke(remoteClient, NOT_USED_ENVELOPE);
    }

    private EndpointDefinition capturedEndpointDefinition() {
        ArgumentCaptor<EndpointDefinition> endpointDefCaptor = ArgumentCaptor.forClass(EndpointDefinition.class);

        verify(restClientProcessor).request(endpointDefCaptor.capture(), any(JsonEnvelope.class));
        return endpointDefCaptor.getValue();
    }

    private Object instanceOf(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "restClientProcessor", restClientProcessor);
        setField(resourceObject, "restClientHelper", restClientHelper);
        return resourceObject;
    }

    private QueryParameter queryParameterOf(String name, boolean required) {
        QueryParameter queryParameter1 = new QueryParameter();
        queryParameter1.setDisplayName(name);
        queryParameter1.setType(ParamType.STRING);
        queryParameter1.setRequired(required);
        return queryParameter1;
    }

}
