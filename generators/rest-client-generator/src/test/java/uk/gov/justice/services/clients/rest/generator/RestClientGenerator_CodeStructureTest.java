package uk.gov.justice.services.clients.rest.generator;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithNoMapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;
import static uk.gov.justice.services.generators.test.utils.builder.QueryParamBuilder.queryParam;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithTitleVersion;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class RestClientGenerator_CodeStructureTest extends BaseGeneratorTest {
    @Before
    public void before() {
        super.before();
        generator = new RestClientGenerator();
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final String GET_MAPPING_ANNOTATION = mappingDescriptionWith(
            mapping()
                    .withResponseType("application/vnd.cakeshop.query.recipe+json")
                    .withName("cakeshop.get-recipe"))
            .build();


    private static final String POST_MAPPING_ANNOTATION = mappingDescriptionWith(
            mapping()
                    .withRequestType("application/vnd.cakeshop.command.update-recipe+json")
                    .withName("cakeshop.update-recipe"))
            .build();

    private static final String BASE_PACKAGE = "org.raml.test";
    private static final Map<String, String> NOT_USED_GENERATOR_PROPERTIES = generatorProperties()
            .withServiceComponentOf("QUERY_CONTROLLER")
            .withActionMappingOf(true)
            .build();

    private static final String BASE_URI_WITH_LESS_THAN_EIGHT_PARTS = "http://localhost:8080/command/api/rest/service";
    private static final String BASE_URI_WITH_MORE_THAN_EIGHT_PARTS = "http://localhost:8080/warname/command/api/rest/service/extra";

    private static final Map<String, String> GENERATOR_PROPERTIES = generatorProperties()
            .withServiceComponentOf("QUERY_API")
            .withActionMappingOf(true)
            .build();

    private static final Map<String, String> QUERY_CONTROLLER_GENERATOR_PROPERTIES = generatorProperties()
            .withServiceComponentOf("QUERY_CONTROLLER")
            .withActionMappingOf(true)
            .build();

    private static final Map<String, String> NO_MAPPING_GENERATOR_PROPERTIES = generatorProperties()
            .withServiceComponentOf("QUERY_CONTROLLER")
            .withActionMappingOf(false)
            .build();

    @Test
    public void shouldGenerateClassWithAnnotations() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .with(resource("/some/path/{recipeId}")
                                .with(httpAction(GET, "application/vnd.cakeshop.query.add-recipe+json")
                                        .with(queryParam("recipename").required(true), queryParam("topingredient").required(false)))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, GENERATOR_PROPERTIES));


        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteServiceQueryApi"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(ServiceComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(ServiceComponent.class).value().toString(), is("QUERY_API"));
        assertBaseUriField(generatedClass.getDeclaredField("BASE_URI"));
        assertRestClientField(generatedClass.getDeclaredField("restClientProcessor"));
        assertRestClientHelperField(generatedClass.getDeclaredField("restClientHelper"));
    }

    @Test
    public void shouldGenerateClassWithQueryControllerAnnotation() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, QUERY_CONTROLLER_GENERATOR_PROPERTIES));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");
        assertThat(generatedClass.getAnnotation(ServiceComponent.class).value().toString(), is("QUERY_CONTROLLER"));
    }

    @Test
    public void shouldCreateLoggerConstant() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, QUERY_CONTROLLER_GENERATOR_PROPERTIES));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceQueryApi");

        Field logger = generatedClass.getDeclaredField("LOGGER");
        assertThat(logger, not(nullValue()));
        assertThat(logger.getType(), equalTo(Logger.class));
        assertThat(Modifier.isPrivate(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isStatic(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isFinal(logger.getModifiers()), Matchers.is(true));
    }

    @Test
    public void shouldGenerateMethodAnnotatedWithHandlesAnnotationForGET() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path/{recipeId}")
                                .with(httpAction(GET)
                                        .withResponseTypes("application/vnd.cakeshop.query.recipe+json")
                                        .withDescription(GET_MAPPING_ANNOTATION))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceCommandApi");
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));

        Method method = methods.get(0);
        Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("cakeshop.get-recipe"));

    }

    @Test
    public void shouldGenerateMethodAnnotatedWithHandlesAnnotationForGETWithNoMapping() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path/{recipeId}")
                                .with(httpActionWithNoMapping(GET)
                                        .withResponseTypes("application/vnd.cakeshop.query.recipe+json"))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NO_MAPPING_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceCommandApi");
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));

        Method method = methods.get(0);
        Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("cakeshop.query.recipe"));

    }

    @Test
    public void shouldGenerateMethodAnnotatedWithHandlesAnnotationForPOST() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path/{recipeId}")
                                .with(httpAction(POST, "application/vnd.cakeshop.command.update-recipe+json")
                                        .withDescription(POST_MAPPING_ANNOTATION))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteServiceCommandApi");
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));

        Method method = methods.get(0);
        Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("cakeshop.update-recipe"));

    }

    @Test
    public void shouldGenerateMethodAcceptingEnvelope() throws MalformedURLException {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path/{recipeId}")
                                .with(httpAction(POST, "application/vnd.cakeshop.command.update-recipe+json")
                                        .withDescription(POST_MAPPING_ANNOTATION))
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
        generator.run(
                restRamlWithDefaults()
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));


    }

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotValid() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("serviceComponent generator property invalid. Expected one of: COMMAND_API, COMMAND_CONTROLLER"));

        Map<String, String> generatorProperties = generatorProperties().withServiceComponentOf("UNKNOWN").build();
        generator.run(
                restRamlWithDefaults()
                        .withDefaultPostResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

    }

    @Test
    public void shouldThrowExceptionIfBaseUriHasLessThanEightParts() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("baseUri must have 8 parts"));

        generator.run(
                restRamlWithTitleVersion().withBaseUri(BASE_URI_WITH_LESS_THAN_EIGHT_PARTS).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, NOT_USED_GENERATOR_PROPERTIES));

    }

    @Test
    public void shouldThrowExceptionIfBaseUriHasMoreThanEightParts() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("baseUri must have 8 parts"));

        generator.run(
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

}
