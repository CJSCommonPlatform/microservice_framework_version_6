package uk.gov.justice.services.generators.commons.client;


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
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.slf4j.Logger;

public class AbstractClientGeneratorTest extends BaseGeneratorTest {

    @Before
    public void before() {
        super.before();
        generator = new TestClientGenerator();
    }


    @Test
    public void shouldGenerateRemoteController() throws Exception {

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpAction(POST, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_API")));


        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteABCController");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteABCController"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_API"));

    }

    @Test
    public void shouldGenerateRemoteHandler() throws Exception {

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpAction(GET)
                                        .withResponseTypes("application/vnd.cakeshop.actionabc+json")))

                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_CONTROLLER")));


        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteABCController");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteABCController"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_CONTROLLER"));

    }



    @Test
    public void shouldContainLoggerConstant() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteABCController");

        Field logger = generatedClass.getDeclaredField("LOGGER");
        assertThat(logger, not(nullValue()));
        assertThat(logger.getType(), equalTo(Logger.class));
        assertThat(Modifier.isPrivate(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isStatic(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isFinal(logger.getModifiers()), Matchers.is(true));
    }

    @Test
    public void shouldContainVariable() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteABCController");

        Field logger = generatedClass.getDeclaredField("dummyVariable");
        assertThat(logger, not(nullValue()));
        assertThat(logger.getType(), equalTo(Object.class));
        assertThat(Modifier.isStatic(logger.getModifiers()), Matchers.is(false));
    }

    @Test
    public void shouldGenerateAnnotatedMethod() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteABCController");

        List<Method> methods = methodsOf(generatedClass);
        assertThat(methods, hasSize(1));

        Method method = methods.get(0);
        Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("some.action"));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonEnvelope.class));
    }

    @Test
    public void methodShouldReturnIntegerValue() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteABCController");

        List<Method> methods = methodsOf(generatedClass);
        assertThat(methods, hasSize(1));
        final Object instance = generatedClass.newInstance();
        Method method = firstMethodOf(generatedClass);
        final Object result = method.invoke(instance, envelope().build());
        assertThat(result, is(12345678));
    }


    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotSet() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("serviceComponent generator property not set in the plugin config");

        Map<String, String> generatorProperties = emptyMap();
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

    }


    @Test
    public void shouldThrowExceptionIfActionOtherThanPOSTorGET() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(containsString("Unsupported httpAction type PUT"));
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpAction(PUT, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

    }

    static class TestClientGenerator extends AbstractClientGenerator {

        @Override
        protected String classNameOf(final Raml raml) {
            return "RemoteABCController";
        }

        @Override
        protected Iterable<FieldSpec> fieldsOf(final Raml raml) {
            return ImmutableList.of(FieldSpec.builder(Object.class, "dummyVariable")
                    .build());
        }

        @Override
        protected TypeName methodReturnTypeOf(final Action ramlAction) {
            return TypeName.INT;
        }


        @Override
        protected CodeBlock methodBodyOf(final Resource resource, final Action ramlAction, final MimeType mimeType) {
            return CodeBlock.builder().addStatement("return 12345678").build();
        }

        @Override
        protected String handlesAnnotationValueOf(final Action ramlAction, final MimeType mimeType, final GeneratorConfig generatorConfig) {
            return "some.action";
        }
    }

}