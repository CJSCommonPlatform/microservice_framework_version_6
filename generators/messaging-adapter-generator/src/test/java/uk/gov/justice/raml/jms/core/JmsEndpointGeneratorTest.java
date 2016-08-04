package uk.gov.justice.raml.jms.core;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.hamcrest.CoreMatchers;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class JmsEndpointGeneratorTest extends BaseGeneratorTest {
    private static final String BASE_PACKAGE = "uk.test";
    private static final String BASE_PACKAGE_FOLDER = "/uk/test";

    @Mock
    JmsProcessor jmsProcessor;

    @Mock
    AsynchronousDispatcher dispatcher;

    @Before
    public void setup() throws Exception {
        super.before();
        generator = new JmsEndpointGenerator();
    }

    @Test
    public void shouldCreateJmsClass() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        File[] files = packageDir.listFiles();
        assertThat(files.length, is(1));
        assertThat(files[0].getName(), is("StructureControllerCommandJmsListener.java"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateMultipleJmsClasses() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .with(resource()
                                .withRelativeUri("/people.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        File[] files = packageDir.listFiles();
        assertThat(files.length, is(2));
        assertThat(files,
                arrayContainingInAnyOrder(hasProperty("name", equalTo("PeopleControllerCommandJmsListener.java")),
                        hasProperty("name", equalTo("StructureControllerCommandJmsListener.java"))));

    }

    @Test
    public void shouldIgnoreGETResource() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.handler.command")
                                .with(httpAction(GET)
                                        .withResponseTypes("application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        File[] files = packageDir.listFiles();
        assertThat(files, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldIgnoreGETResourceInMultipleResourceRaml() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .with(resource()
                                .withRelativeUri("/cakeshop.handler.command")
                                .with(httpAction(GET)
                                        .withResponseTypes("application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        File[] files = packageDir.listFiles();
        assertThat(files.length, is(1));
        assertThat(files[0].getName(), is("StructureControllerCommandJmsListener.java"));

    }


    @Test
    public void shouldOverwriteJmsClass() throws Exception {
        String path = outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER;
        File packageDir = new File(path);
        packageDir.mkdirs();
        Files.write(Paths.get(path + "/StructureControllerCommandJmsListener.java"),
                Collections.singletonList("Old file content"));

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        List<String> lines = Files.readAllLines(Paths.get(path + "/StructureControllerCommandJmsListener.java"));
        assertThat(lines.get(0), not(containsString("Old file content")));
    }

    @Test
    public void shouldCreateJmsEndpointNamedAfterResourceUri() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass("uk.somepackage", "StructureControllerCommandJmsListener");
        assertThat(clazz.getName(), is("uk.somepackage.StructureControllerCommandJmsListener"));
    }

    @Test
    public void shouldCreateLoggerConstant() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, emptyMap()));

        Class<?> resourceClass = getJmsListenerClass("uk.somepackage", "StructureControllerCommandJmsListener");

        Field logger = resourceClass.getDeclaredField("LOGGER");
        assertThat(logger, CoreMatchers.not(nullValue()));
        assertThat(logger.getType(), CoreMatchers.equalTo(Logger.class));
        assertThat(Modifier.isPrivate(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isStatic(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isFinal(logger.getModifiers()), Matchers.is(true));
    }

    @Test
    public void shouldCreateJmsEndpointInADifferentPackage() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.package2", outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass("uk.package2", "StructureControllerCommandJmsListener");
        assertThat(clazz.getName(), is("uk.package2.StructureControllerCommandJmsListener"));
    }

    @Test
    public void shouldCreateJmsEventProcessorNamedAfterResourceUriAndBaseUri() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, emptyMap()));

        Class<?> compiledClass = getJmsListenerClass("uk.somepackage", "StructureEventJmsListener");
        assertThat(compiledClass.getName(), is("uk.somepackage.StructureEventJmsListener"));
    }


    @Test
    public void shouldCreateJmsEndpointAnnotatedWithCommandHandlerAdapter() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .withBaseUri("message://command/handler/message/abc")
                        .with(resource()
                                .withRelativeUri("/people.some.queue")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleSomeQueueJmsListener");
        Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(COMMAND_HANDLER));
    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithControllerCommandAdapter() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .withBaseUri("message://command/controller/message/abc")
                        .with(resource()
                                .withRelativeUri("/people.some.query")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleSomeQueryJmsListener");
        Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(COMMAND_CONTROLLER));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithEventListenerAdapter() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/people")
                        .with(resource()
                                .withRelativeUri("/people.event")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleEventJmsListener");
        Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(Component.EVENT_LISTENER));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithEventProcessorAdapter() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/processor/message/people")
                        .with(resource()
                                .withRelativeUri("/people.event")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleEventJmsListener");
        Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(Component.EVENT_PROCESSOR));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithInterceptors() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/people.handler.command")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleHandlerCommandJmsListener");
        Interceptors interceptorsAnnotation = clazz.getAnnotation(Interceptors.class);
        assertThat(interceptorsAnnotation, not(nullValue()));
        assertThat(interceptorsAnnotation.value(), hasItemInArray(JsonSchemaValidationInterceptor.class));
    }

    @Test
    public void shouldCreateJmsEndpointWithoutInterceptorsIfMediaTypeIsGeneralJson() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/people.handler.command")
                                .with(httpAction(POST, "application/json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleHandlerCommandJmsListener");
        Interceptors interceptorsAnnotation = clazz.getAnnotation(Interceptors.class);
        assertThat(interceptorsAnnotation, nullValue());

    }


    @Test
    public void shouldCreateJmsEndpointImplementingMessageListener() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "SomecontextControllerCommandJmsListener");
        assertThat(clazz.getInterfaces().length, equalTo(1));
        assertThat(clazz.getInterfaces()[0], equalTo(MessageListener.class));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedDispatcherProperty() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "SomecontextControllerCommandJmsListener");
        Field dispatcherField = clazz.getDeclaredField("dispatcher");
        assertThat(dispatcherField, not(nullValue()));
        assertThat(dispatcherField.getType(), CoreMatchers.equalTo((AsynchronousDispatcher.class)));
        assertThat(dispatcherField.getAnnotations(), arrayWithSize(1));
        assertThat(dispatcherField.getAnnotation(Inject.class), not(nullValue()));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedJmsProcessorProperty() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "SomecontextControllerCommandJmsListener");
        Field jmsProcessorField = clazz.getDeclaredField("jmsProcessor");
        assertThat(jmsProcessorField, not(nullValue()));
        assertThat(jmsProcessorField.getType(), CoreMatchers.equalTo((JmsProcessor.class)));
        assertThat(jmsProcessorField.getAnnotations(), arrayWithSize(1));
        assertThat(jmsProcessorField.getAnnotation(Inject.class), not(nullValue()));
    }

    @Test
    public void shouldCreateAnnotatedCommandControllerEndpointWithDestinationLookupProperty() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/people.controller.command")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleControllerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("people.controller.command")))));
    }

    @Test
    public void shouldCreateAnnotatedCommandControllerEndpointWithDestinationLookupProperty2() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .with(httpAction(POST, "application/vnd.structure.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureControllerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("structure.controller.command")))));
    }

    @Test
    public void shouldCreateAnnotatedCommandHandlerEndpointWithDestinationLookupProperty3() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.handler.command")
                                .with(httpAction(POST, "application/vnd.structure.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureHandlerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("structure.handler.command")))));
    }

    @Test
    public void shouldCreateAnnotatedEventListenerEndpointWithDestinationLookupProperty3() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .withDefaultMessagingBaseUri()
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .with(httpAction(POST, "application/vnd.structure.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("structure.event")))));
    }

    @Test
    public void shouldCreateAnnotatedControllerCommandEndpointWithQueueAsDestinationType() throws Exception {
        generator.run(raml()
                        .withBaseUri("message://command/controller/message/people")
                        .with(resource()
                                .withRelativeUri("/structure.something")
                                .with(httpAction(POST, "application/vnd.structure.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureSomethingJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationType")),
                        propertyValue(equalTo("javax.jms.Queue")))));
    }

    @Test
    public void shouldCreateAnnotatedCommandHandlerEndpointWithQueueAsDestinationType() throws Exception {
        generator.run(raml()
                        .withBaseUri("message://command/handler/message/aaa")
                        .with(resource()
                                .withRelativeUri("/lifecycle.blah")
                                .with(httpAction(POST, "application/vnd.lifecycle.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "LifecycleBlahJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationType")),
                        propertyValue(equalTo("javax.jms.Queue")))));
    }

    @Test
    public void shouldCreateAnnotatedEventListenerEndpointWithQueueAsDestinationType() throws Exception {
        generator.run(messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/people.event")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationType")),
                        propertyValue(equalTo("javax.jms.Topic")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithMessageSelectorContainingOneCommandWithAPost() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/vnd.structure.test-cmd+json", "json/schema/test-cmd.json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureControllerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-cmd')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointFromMediaTypeWithoutPillar() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/vnd.structure.test-cmd+json", "json/schema/test-cmd.json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureControllerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-cmd')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithMessageSelectorContainingOneEvent() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/vnd.structure.test-event+json", "json/schema/test-event.json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-event')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithMessageSelectorContainingOneEvent_PluralPillarName() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/vnd.structure.events.test-event+json", "json/schema/test-event.json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.events.test-event')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithoutMessageSelectorIfEventNameNotSpecifiedInMediaType() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/public.event")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/json", "json/schema/application.json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PublicEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                not(hasItemInArray(propertyName(equalTo("messageSelector")))));
    }

    @Test
    public void shouldOnlyCreateMessageSelectorForPostActionAndIgnoreAllOtherActions() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .with(httpAction(POST, "application/vnd.structure.test-cmd1+json"))
                                .with(httpAction(GET, "application/vnd.structure.test-cmd2+json"))
                                .with(httpAction(DELETE, "application/vnd.structure.test-cmd3+json"))
                                .with(httpAction(HEAD, "application/vnd.structure.test-cmd4+json"))
                                .with(httpAction(OPTIONS, "application/vnd.structure.test-cmd5+json"))
                                .with(httpAction(PATCH, "application/vnd.structure.test-cmd6+json"))
                                .with(httpAction(TRACE, "application/vnd.structure.test-cmd7+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "StructureControllerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-cmd1')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithMessageSelectorContainingTwoCommand() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/people.controller.command")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/vnd.people.command1+json", "json/schema/command1.json")
                                        .withMediaType("application/vnd.people.command2+json", "json/schema/command2.json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleControllerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(startsWith("CPPNAME in")),
                        propertyValue(allOf(containsString("'people.command1'"),
                                containsString("'people.command2'"))))));
    }

    @Test
    public void shouldCreateJmsEndpointWithOnMessage() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "SomecontextControllerCommandJmsListener");

        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), CoreMatchers.equalTo(void.class));
        assertThat(method.getParameterCount(), Matchers.is(1));
        assertThat(method.getParameters()[0].getType(), CoreMatchers.equalTo(Message.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallJmsProcessorWhenOnMessageIsInvoked() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "SomecontextControllerCommandJmsListener");
        Object object = instantiate(clazz);
        assertThat(object, is(instanceOf(MessageListener.class)));

        MessageListener jmsListener = (MessageListener) object;
        Message message = mock(Message.class);
        jmsListener.onMessage(message);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(jmsProcessor).process(consumerCaptor.capture(), eq(message));

        JsonEnvelope envelope = envelope().build();
        consumerCaptor.getValue().accept(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @Test
    public void shouldCreateDurableTopicSubscriber() throws Exception {
        generator.run(raml()
                        .withBaseUri("message://event/listener/message/people")
                        .with(resource()
                                .withRelativeUri("/people.event")
                                .with(httpAction(POST, "application/vnd.context1.event.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleEventJmsListener");
        ActivationConfigProperty[] activationConfig = clazz.getAnnotation(MessageDriven.class).activationConfig();
        assertThat(activationConfig, hasItemInArray(
                allOf(propertyName(equalTo("destinationType")), propertyValue(equalTo("javax.jms.Topic")))));
        assertThat(activationConfig, hasItemInArray(
                allOf(propertyName(equalTo("subscriptionDurability")), propertyValue(equalTo("Durable")))));
        assertThat(activationConfig, hasItemInArray(
                allOf(propertyName(equalTo("clientId")), propertyValue(equalTo("people.event.listener")))));
        assertThat(activationConfig, hasItemInArray(
                allOf(propertyName(equalTo("subscriptionName")), propertyValue(equalTo("people.event.listener.people.event")))));

    }

    @Test
    public void shouldNotContainDurableSubscriberPropertiesIfItsNotTopic() throws Exception {
        generator.run(raml()
                        .withBaseUri("message://command/controller/message/people")
                        .with(resource()
                                .withRelativeUri("/people.controller.command")
                                .with(httpAction(POST, "application/vnd.people.event.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = getJmsListenerClass(BASE_PACKAGE, "PeopleControllerCommandJmsListener");
        ActivationConfigProperty[] activationConfig = clazz.getAnnotation(MessageDriven.class).activationConfig();
        assertThat(activationConfig, hasItemInArray(allOf(
                propertyName(equalTo("destinationType")),
                propertyValue(equalTo("javax.jms.Queue")))));
        assertThat(activationConfig, not(hasItemInArray(allOf(
                propertyName(equalTo("clientId")),
                propertyName(equalTo("subscriptionName")),
                propertyName(equalTo("subscriptionDurability"))))));
    }


    private Object instantiate(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "jmsProcessor", jmsProcessor);
        setField(resourceObject, "dispatcher", dispatcher);
        return resourceObject;
    }

    private FeatureMatcher<ActivationConfigProperty, String> propertyName(Matcher<String> matcher) {
        return new FeatureMatcher<ActivationConfigProperty, String>(matcher, "propertyName", "propertyName") {
            @Override
            protected String featureValueOf(ActivationConfigProperty actual) {
                return actual.propertyName();
            }
        };
    }

    private FeatureMatcher<ActivationConfigProperty, String> propertyValue(Matcher<String> matcher) {
        return new FeatureMatcher<ActivationConfigProperty, String>(matcher, "propertyValue", "propertyValue") {
            @Override
            protected String featureValueOf(ActivationConfigProperty actual) {
                return actual.propertyValue();
            }
        };
    }

    private Class<?> getJmsListenerClass(final String packageName, final String className) throws Exception {
        Set<Class<?>> compiledClasses = compiler.compiledClassesOf(packageName);
        return compiler.classOf(compiledClasses, packageName, className);
    }
}
