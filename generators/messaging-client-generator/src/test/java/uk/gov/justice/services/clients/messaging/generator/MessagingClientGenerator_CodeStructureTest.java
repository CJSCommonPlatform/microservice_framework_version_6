package uk.gov.justice.services.clients.messaging.generator;


import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

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
import org.slf4j.Logger;

public class MessagingClientGenerator_CodeStructureTest extends BaseGeneratorTest {
    @Before
    public void before() {
        super.before();
        generator = new MessagingClientGenerator();
    }

    @Test
    public void shouldGenerateClassWithAnnotations() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_API")));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorCakeshopControllerCommand");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteContextEventProcessorCakeshopControllerCommand"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_API"));

    }

    @Test
    public void shouldGenerateClassWithCommandControllerAnnotation() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.handler.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_CONTROLLER")));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorCakeshopHandlerCommand");

        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_CONTROLLER"));
    }

    @Test
    public void shouldGenerateClientForEventTopic() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/public.event")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("EVENT_PROCESSOR")));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorPublicEvent");

        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("EVENT_PROCESSOR"));
    }

    @Test
    public void shouldCreateLoggerConstant() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorCakeshopControllerCommand");

        Field logger = generatedClass.getDeclaredField("LOGGER");
        assertThat(logger, not(nullValue()));
        assertThat(logger.getType(), equalTo(Logger.class));
        assertThat(Modifier.isPrivate(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isStatic(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isFinal(logger.getModifiers()), Matchers.is(true));
    }

    @Test
    public void shouldCreateJmsEnvelopeSenderVariable() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorCakeshopControllerCommand");

        Field sender = generatedClass.getDeclaredField("sender");
        assertThat(sender, not(nullValue()));
        assertThat(sender.getAnnotation(Inject.class), not(nullValue()));
        assertThat(sender.getType(), equalTo(JmsEnvelopeSender.class));
        assertThat(Modifier.isStatic(sender.getModifiers()), Matchers.is(false));

    }

    @Test
    public void shouldGenerateMethodAnnotatedWithHandlesAnnotation() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.controller.command")
                                .with(httpActionWithDefaultMapping(POST, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorCakeshopControllerCommand");

        List<Method> methods = methodsOf(generatedClass);
        assertThat(methods, hasSize(1));

        Method method = methods.get(0);
        Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("cakeshop.actionabc"));
    }

    @Test
    public void shouldGenerateMethodAnnotatedWithHandlesAnnotationForGET() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.handler.command")
                                .with(httpActionWithDefaultMapping(GET)
                                        .withResponseTypes("application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

        Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorCakeshopHandlerCommand");

        List<Method> methods = methodsOf(generatedClass);
        assertThat(methods, hasSize(1));

        Method method = methods.get(0);
        Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("cakeshop.actionabc"));
    }

    @Test
    public void shouldGenerateMethodAcceptingEnvelope() throws MalformedURLException {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.controller.command")
                                .with(httpActionWithDefaultMapping(POST, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextEventProcessorCakeshopControllerCommand");
        Method method = firstMethodOf(clazz);
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonEnvelope.class));
    }

    @Test
    public void shouldGenerateClassIfServiceNameContainsHyphens() throws MalformedURLException {
        generator.run(
                messagingRamlWithDefaults()
                        .withBaseUri("message://event/processor/message/context-with-hyphens")
                        .with(resource()
                                .withRelativeUri("/cakeshop.controller.command")
                                .with(httpActionWithDefaultMapping(POST, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

        compiler.compiledClassOf(BASE_PACKAGE, "RemoteContextWithHyphensEventProcessorCakeshopControllerCommand");
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



}
