package uk.gov.justice.raml.jms.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataInterceptor;
import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.adapter.messaging.MessagingJsonSchemaValidationService;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
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
import java.util.function.Consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.everit.json.schema.ValidationException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jboss.ejb3.annotation.Pool;
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
    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";

    @Mock
    JmsProcessor jmsProcessor;

    @Mock
    MessagingJsonSchemaValidationService jsonSchemaValidationService;

    @Mock
    InterceptorChainProcessor interceptorChainProcessor;

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
        assertThat(asList(packageDir.listFiles()),
                hasItem(hasProperty("name", equalTo("ContextEventProcessorStructureControllerCommandJmsListener.java"))));
    }

    @Test
    public void shouldCreateClassIfBaseUriContainsHyphens() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .withBaseUri("message://event/processor/message/context-with-hyphens")
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, emptyMap()));

        Class<?> compiledClass = compiler.compiledClassOf("uk.somepackage", "ContextWithHyphensEventProcessorStructureEventJmsListener");
        assertThat(compiledClass.getName(), is("uk.somepackage.ContextWithHyphensEventProcessorStructureEventJmsListener"));
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
        assertThat(asList(packageDir.listFiles()),
                hasItems(hasProperty("name", equalTo("ContextEventProcessorPeopleControllerCommandJmsListener.java")),
                        hasProperty("name", equalTo("ContextEventProcessorStructureControllerCommandJmsListener.java"))));

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
        assertThat(asList(files), hasItem(hasProperty("name", containsString("StructureControllerCommandJmsListener"))));
        assertThat(asList(files), not(hasItem(hasProperty("name", containsString("Cakeshop")))));

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

        List<String> lines = Files.readAllLines(Paths.get(path + "/ContextEventProcessorStructureControllerCommandJmsListener.java"));
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

        Class<?> clazz = compiler.compiledClassOf("uk.somepackage", "ContextEventProcessorStructureControllerCommandJmsListener");
        assertThat(clazz.getName(), is("uk.somepackage.ContextEventProcessorStructureControllerCommandJmsListener"));
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

        Class<?> resourceClass = compiler.compiledClassOf("uk.somepackage", "ContextEventProcessorStructureControllerCommandJmsListener");

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

        Class<?> clazz = compiler.compiledClassOf("uk.package2", "ContextEventProcessorStructureControllerCommandJmsListener");
        assertThat(clazz.getName(), is("uk.package2.ContextEventProcessorStructureControllerCommandJmsListener"));
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

        Class<?> compiledClass = compiler.compiledClassOf("uk.somepackage", "ContextEventProcessorStructureEventJmsListener");
        assertThat(compiledClass.getName(), is("uk.somepackage.ContextEventProcessorStructureEventJmsListener"));
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
        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "AbcCommandHandlerPeopleSomeQueueJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "AbcCommandControllerPeopleSomeQueryJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeopleEventJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventProcessorPeopleEventJmsListener");
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
        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleHandlerCommandJmsListener");
        Interceptors interceptorsAnnotation = clazz.getAnnotation(Interceptors.class);
        assertThat(interceptorsAnnotation, not(nullValue()));
        assertThat(interceptorsAnnotation.value(), hasItemInArray(JmsLoggerMetadataInterceptor.class));
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
        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleHandlerCommandJmsListener");
        Interceptors interceptorsAnnotation = clazz.getAnnotation(Interceptors.class);
        assertThat(interceptorsAnnotation, nullValue());

    }


    @Test
    public void shouldCreateJmsEndpointImplementingMessageListener() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
        assertThat(clazz.getInterfaces().length, equalTo(1));
        assertThat(clazz.getInterfaces()[0], equalTo(MessageListener.class));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedInterceptorChainProcessorProperty() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
        Field chainProcessField = clazz.getDeclaredField(INTERCEPTOR_CHAIN_PROCESSOR);
        assertThat(chainProcessField, not(nullValue()));
        assertThat(chainProcessField.getType(), CoreMatchers.equalTo((InterceptorChainProcessor.class)));
        assertThat(chainProcessField.getAnnotations(), arrayWithSize(1));
        assertThat(chainProcessField.getAnnotation(Inject.class), not(nullValue()));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedJmsProcessorProperty() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleControllerCommandJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureHandlerCommandJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureEventJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleCommandControllerStructureSomethingJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "AaaCommandHandlerLifecycleBlahJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleEventJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-cmd')")))));
    }

    @Test
    public void shouldNotAddMessageSelectorForEventListener() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/context")
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/vnd.structure.test-event+json", "json/schema/test-event.json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventListenerStructureEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                not(hasItemInArray(propertyName(equalTo("messageSelector")))));
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.events.test-event')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithoutMessageSelectorIfEventNameNotSpecifiedInMediaType() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/processor/message/context")
                        .with(resource()
                                .withRelativeUri("/some.event")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomeEventJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleControllerCommandJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");

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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
        Object object = instantiate(clazz);
        assertThat(object, is(instanceOf(MessageListener.class)));

        MessageListener jmsListener = (MessageListener) object;
        Message message = mock(Message.class);

        jmsListener.onMessage(message);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(jmsProcessor).process(consumerCaptor.capture(), eq(message));

        JsonEnvelope envelope = envelope().build();
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope);
        consumerCaptor.getValue().accept(interceptorContext);

        verify(interceptorChainProcessor).process(interceptorContext);
    }

    @Test(expected = ValidationException.class)
    @SuppressWarnings("unchecked")
    public void shouldNotCallJmsProcessorWhenOnMessageIsInvokedWithInvalidJson() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
        Object object = instantiate(clazz);
        assertThat(object, is(instanceOf(MessageListener.class)));

        MessageListener jmsListener = (MessageListener) object;
        Message message = mock(Message.class);

        doThrow(new ValidationException("")).when(jsonSchemaValidationService).validate(message, "EVENT_PROCESSOR");

        jmsListener.onMessage(message);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(jmsProcessor, never()).process(consumerCaptor.capture(), eq(message));
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeopleEventJmsListener");
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

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleCommandControllerPeopleControllerCommandJmsListener");
        ActivationConfigProperty[] activationConfig = clazz.getAnnotation(MessageDriven.class).activationConfig();
        assertThat(activationConfig, hasItemInArray(allOf(
                propertyName(equalTo("destinationType")),
                propertyValue(equalTo("javax.jms.Queue")))));
        assertThat(activationConfig, not(hasItemInArray(allOf(
                propertyName(equalTo("clientId")),
                propertyName(equalTo("subscriptionName")),
                propertyName(equalTo("subscriptionDurability"))))));
    }

    @Test
    public void shouldCreateAnnotatedEventListenerEndpointWithSharedSubscriptionsPropertySetToTrue() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .withDefaultMessagingBaseUri()
                        .with(resource()
                                .withRelativeUri("/structure.event")
                                .with(httpAction(POST, "application/vnd.structure.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureEventJmsListener");
        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("shareSubscriptions")),
                        propertyValue(equalTo("true")))));
    }


    @Test
    public void shouldCreateJmsEndpointAnnotatedWithPoolConfiguration() throws Exception {
        generator.run(raml()
                        .withBaseUri("message://event/listener/message/people")
                        .with(resource()
                                .withRelativeUri("/people.person-added")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withCustomMDBPool()));
        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeoplePersonAddedJmsListener");
        Pool poolAnnotation = clazz.getAnnotation(Pool.class);
        assertThat(poolAnnotation, not(nullValue()));
        assertThat(poolAnnotation.value(), is("people-person-added-event-listener-pool"));
    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithoutPoolConfiguration() throws Exception {
        generator.run(raml()
                        .withBaseUri("message://event/listener/message/people")
                        .with(resource()
                                .withRelativeUri("/people.person-added")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeoplePersonAddedJmsListener");
        Pool poolAnnotation = clazz.getAnnotation(Pool.class);
        assertThat(poolAnnotation, nullValue());

    }

    private Object instantiate(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "jmsProcessor", jmsProcessor);
        setField(resourceObject, "jsonSchemaValidationService", jsonSchemaValidationService);
        setField(resourceObject, INTERCEPTOR_CHAIN_PROCESSOR, interceptorChainProcessor);
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

}
