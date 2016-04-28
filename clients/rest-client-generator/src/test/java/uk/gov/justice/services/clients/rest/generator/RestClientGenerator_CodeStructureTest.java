package uk.gov.justice.services.clients.rest.generator;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithTitleVersion;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.raml.model.ParamType;
import org.raml.model.parameter.QueryParameter;

public class RestClientGenerator_CodeStructureTest {

    private static final String BASE_PACKAGE = "org.raml.test";
    private static final Map<String, String> NOT_USED_GENERATOR_PROPERTIES = ImmutableMap.of("serviceComponent", "QUERY_CONTROLLER");
    private static final String BASE_URI_WITH_LESS_THAN_EIGHT_PARTS = "http://localhost:8080/command/api/rest/service";
    private static final String BASE_URI_WITH_MORE_THAN_EIGHT_PARTS = "http://localhost:8080/warname/command/api/rest/service/extra";
    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RestClientGenerator restClientGenerator;
    private JavaCompilerUtil compiler;

    @Before
    public void before() {
        restClientGenerator = new RestClientGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldGenerateClassWithAnnotations() throws Exception {

        restClientGenerator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .with(resource("/some/path/{recipeId}")
                                .with(action(POST, "application/vnd.cakeshop.commands.add-recipe+json")
                                        .withQueryParameters(queryParameterOf("recipename", true), queryParameterOf("topingredient", false))
                                        .withActionWithResponseTypes("application/vnd.cakeshop.commands.cmd1+json"))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ImmutableMap.of("serviceComponent", "QUERY_API")));


        Class<?> applicationClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");

        assertThat(applicationClass.getCanonicalName(), is("org.raml.test.RemoteServiceQueryApi"));
        assertThat(applicationClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(applicationClass.getAnnotation(ServiceComponent.class), not(nullValue()));
        assertThat(applicationClass.getAnnotation(ServiceComponent.class).value().toString(), is("QUERY_API"));
        assertBaseUriField(applicationClass.getDeclaredField("BASE_URI"));
        assertRestClientField(applicationClass.getDeclaredField("restClientProcessor"));
        assertRestClientHelperField(applicationClass.getDeclaredField("restClientHelper"));
    }

    @Test
    public void shouldGenerateClassWithQueryControllerAnnotation() throws Exception {
        restClientGenerator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ImmutableMap.of("serviceComponent", "QUERY_CONTROLLER")));

        Class<?> applicationClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");
        assertThat(applicationClass.getAnnotation(ServiceComponent.class).value().toString(), is("QUERY_CONTROLLER"));
    }

    @Test
    public void shouldGenerateMethodAnnotatedWithHandlesAnnotation() throws Exception {
        restClientGenerator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path/{recipeId}")
                                .with(action(POST, "application/vnd.cakeshop.commands.update-recipe+json"))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceCommandApi");
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));

        Method method = methods.get(0);
        Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("cakeshop.commands.update-recipe"));

    }

    @Test
    public void shouldGenerateMethodAcceptingEnvelope() throws MalformedURLException {
        restClientGenerator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path/{recipeId}")
                                .with(action(POST, "application/vnd.cakeshop.commands.update-recipe+json"))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceCommandApi");
        Method method = firstMethodOf(clazz);
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo((JsonEnvelope.class)));
    }

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotSet() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("serviceComponent generator property not set in the plugin config");

        Map<String, String> generatorProperties = emptyMap();
        restClientGenerator.run(
                restRamlWithDefaults()
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));


    }

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotValid() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("serviceComponent generator property invalid. Expected one of: COMMAND_API, COMMAND_CONTROLLER"));

        Map<String, String> generatorProperties = ImmutableMap.of("serviceComponent", "UNKNOWN");
        restClientGenerator.run(
                restRamlWithDefaults()
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

    }

    @Test
    public void shouldThrowExceptionIfBaseUriHasLessThanEightParts() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("baseUri must have 8 parts"));

        restClientGenerator.run(
                restRamlWithTitleVersion().withBaseUri(BASE_URI_WITH_LESS_THAN_EIGHT_PARTS).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

    }

    @Test
    public void shouldThrowExceptionIfBaseUriHasMoreThanEightParts() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("baseUri must have 8 parts"));

        restClientGenerator.run(
                restRamlWithTitleVersion().withBaseUri(BASE_URI_WITH_MORE_THAN_EIGHT_PARTS).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

    }

    private void assertBaseUriField(Field field) {
        assertThat(Modifier.isStatic(field.getModifiers()), is(true));
        assertThat(Modifier.isPrivate(field.getModifiers()), is(true));
        assertThat(Modifier.isFinal(field.getModifiers()), is(true));
    }

    private void assertRestClientField(Field field) {
        assertThat(field.getAnnotation(Inject.class), not(nullValue()));
    }

    private void assertRestClientHelperField(Field field) {
        assertThat(field.getAnnotation(Inject.class), not(nullValue()));
    }


    private QueryParameter queryParameterOf(String name, boolean required) {
        QueryParameter queryParameter1 = new QueryParameter();
        queryParameter1.setDisplayName(name);
        queryParameter1.setType(ParamType.STRING);
        queryParameter1.setRequired(required);
        return queryParameter1;
    }


}
