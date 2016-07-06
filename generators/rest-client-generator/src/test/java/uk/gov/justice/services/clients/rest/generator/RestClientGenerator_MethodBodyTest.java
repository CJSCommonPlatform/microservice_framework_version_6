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
import static org.raml.model.ParamType.BOOLEAN;
import static org.raml.model.ParamType.INTEGER;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.QueryParamBuilder.queryParam;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultGetResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.adapter.rest.parameter.ParameterType;
import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestClientGenerator_MethodBodyTest extends BaseGeneratorTest {

    private static final JsonEnvelope NOT_USED_ENVELOPE = envelope().build();
    private static final Map<String, String> NOT_USED_GENERATOR_PROPERTIES = generatorProperties().withServiceComponentOf("QUERY_CONTROLLER").build();

    @Mock
    RestClientProcessor restClientProcessor;
    @Mock
    RestClientHelper restClientHelper;

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteService1QueryApi");
        invokeFirstMethod(clazz);

        assertThat(capturedEndpointDefinition().getBaseUri(), is("http://localhost:8080/contextabc/query/api/rest/service1"));
    }

    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingPath() throws Exception {

        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/pathabc/{anId}").with(httpAction().withHttpActionType(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");
        invokeFirstMethod(clazz);
        assertThat(capturedEndpointDefinition().getPath(), is("/pathabc/{anId}"));
    }


    @Test
    public void shouldCallRestClientWithEndpointDefinitionContainingPathParams() throws Exception {

        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/pathabc").with(httpAction().withHttpActionType(GET).withDefaultResponseType()))
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");

        invokeFirstMethod(clazz);

        EndpointDefinition endpointDefinition = capturedEndpointDefinition();

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");
        Object remoteClient = instanceOf(clazz);
        Method method = firstMethodOf(clazz);

        JsonEnvelope envelope = envelope().build();
        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).get(any(EndpointDefinition.class), same(envelope));
    }

    @Test
    public void shouldPassPostEnvelopeToRestClient() throws Exception {

        generator.run(
                restRamlWithQueryApiDefaults()
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");
        Object remoteClient = instanceOf(clazz);
        Method method = firstMethodOf(clazz);

        JsonEnvelope envelope = envelope().build();
        method.invoke(remoteClient, envelope);

        verify(restClientProcessor).post(any(EndpointDefinition.class), same(envelope));
    }

    private void invokeFirstMethod(final Class<?> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object remoteClient = instanceOf(clazz);
        Method method = firstMethodOf(clazz);
        method.invoke(remoteClient, NOT_USED_ENVELOPE);
    }

    private EndpointDefinition capturedEndpointDefinition() {
        ArgumentCaptor<EndpointDefinition> endpointDefCaptor = ArgumentCaptor.forClass(EndpointDefinition.class);

        verify(restClientProcessor).get(endpointDefCaptor.capture(), any(JsonEnvelope.class));
        return endpointDefCaptor.getValue();
    }

    private Object instanceOf(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "restClientProcessor", restClientProcessor);
        setField(resourceObject, "restClientHelper", restClientHelper);
        return resourceObject;
    }

}
