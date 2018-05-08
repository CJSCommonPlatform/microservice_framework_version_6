package uk.gov.justice.raml.jms.core;

import static java.util.Arrays.asList;
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
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;
import uk.gov.justice.raml.jms.config.GeneratorPropertiesFactory;
import uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataInterceptor;
import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility;

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

import org.hamcrest.CoreMatchers;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jboss.ejb3.annotation.Pool;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Raml;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class JmsEndpointGeneratorTest {
    private static final String BASE_PACKAGE = "uk.test";
    private static final String BASE_PACKAGE_FOLDER = "/uk/test";
    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";
    private static final JavaCompilerUtility COMPILER = javaCompilerUtil();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private JmsProcessor jmsProcessor;

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    private GeneratorProperties generatorProperties;
    private Generator<Raml> generator;

    @Before
    public void setup() throws Exception {
        generator = new JmsEndpointGenerator();
        generatorProperties = new GeneratorPropertiesFactory().withDefaultServiceComponent();
    }

    @Test
    public void shouldCreateJmsClass() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
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
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> compiledClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                "uk.somepackage",
                "ContextWithHyphensEventProcessorStructureEventJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        final File[] a = packageDir.listFiles();
        assertThat(asList(a),
                hasItems(hasProperty("name", equalTo("ContextEventProcessorPeopleControllerCommandJmsListener.java")),
                        hasProperty("name", equalTo("ContextEventProcessorStructureControllerCommandJmsListener.java"))
                ));
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        final File[] files = packageDir.listFiles();
        assertThat(files.length, is(1));
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        final File[] files = packageDir.listFiles();
        assertThat(asList(files), hasItem(hasProperty("name", containsString("StructureControllerCommandJmsListener"))));
        assertThat(asList(files), not(hasItem(hasProperty("name", containsString("Cakeshop")))));

    }


    @Test
    public void shouldOverwriteJmsClass() throws Exception {
        final String path = outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER;
        final File packageDir = new File(path);
        packageDir.mkdirs();
        Files.write(Paths.get(path + "/StructureControllerCommandJmsListener.java"),
                Collections.singletonList("Old file content"));

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/structure.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final List<String> lines = Files.readAllLines(Paths.get(path + "/ContextEventProcessorStructureControllerCommandJmsListener.java"));
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
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                "uk.somepackage",
                "ContextEventProcessorStructureControllerCommandJmsListener");

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
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                "uk.somepackage",
                "ContextEventProcessorStructureControllerCommandJmsListener");

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
                configurationWithBasePackage("uk.package2", outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                "uk.package2",
                "ContextEventProcessorStructureControllerCommandJmsListener");

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
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> compiledClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                "uk.somepackage",
                "ContextEventProcessorStructureEventJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(COMMAND_HANDLER)));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "AbcCommandHandlerPeopleSomeQueueJmsListener");

        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(COMMAND_CONTROLLER)));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "AbcCommandControllerPeopleSomeQueryJmsListener");

        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_LISTENER)));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "PeopleEventListenerPeopleEventJmsListener");

        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(Component.EVENT_LISTENER));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithCustomEventListenerAdapter() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/custom")
                        .with(resource()
                                .withRelativeUri("/people.event")
                                .with(httpAction(POST, "application/vnd.people.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf("CUSTOM_EVENT_LISTENER")));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "CustomEventListenerPeopleEventJmsListener");

        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is("CUSTOM_EVENT_LISTENER"));

        final Class<?> customEventFilterInterceptor = COMPILER.compiledClassOf(outputFolder.getRoot(), outputFolder.getRoot(), BASE_PACKAGE, "CustomEventListenerPeopleEventEventFilterInterceptor");
        final Field eventFilter = customEventFilterInterceptor.getDeclaredField("eventFilter");
        final Class<?> customEventFilterClass = eventFilter.getType();

        assertThat(customEventFilterClass.getName(), is("uk.test.CustomEventListenerPeopleEventEventFilter"));
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_PROCESSOR)));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "PeopleEventProcessorPeopleEventJmsListener");

        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorPeopleHandlerCommandJmsListener");

        final Interceptors interceptorsAnnotation = clazz.getAnnotation(Interceptors.class);
        assertThat(interceptorsAnnotation, not(nullValue()));
        assertThat(interceptorsAnnotation.value(), hasItemInArray(JsonSchemaValidationInterceptor.class));
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorPeopleHandlerCommandJmsListener");

        final Interceptors interceptorsAnnotation = clazz.getAnnotation(Interceptors.class);
        assertThat(interceptorsAnnotation, nullValue());

    }


    @Test
    public void shouldCreateJmsEndpointImplementingMessageListener() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorSomecontextControllerCommandJmsListener");

        assertThat(clazz.getInterfaces().length, equalTo(1));
        assertThat(clazz.getInterfaces()[0], equalTo(MessageListener.class));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedInterceptorChainProcessorProperty() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorSomecontextControllerCommandJmsListener");

        final Field chainProcessField = clazz.getDeclaredField(INTERCEPTOR_CHAIN_PROCESSOR);
        assertThat(chainProcessField, not(nullValue()));
        assertThat(chainProcessField.getType(), CoreMatchers.equalTo((InterceptorChainProcessor.class)));
        assertThat(chainProcessField.getAnnotations(), arrayWithSize(1));
        assertThat(chainProcessField.getAnnotation(Inject.class), not(nullValue()));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedJmsProcessorProperty() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorSomecontextControllerCommandJmsListener");

        final Field jmsProcessorField = clazz.getDeclaredField("jmsProcessor");
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorPeopleControllerCommandJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureControllerCommandJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureHandlerCommandJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureEventJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "PeopleCommandControllerStructureSomethingJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "AaaCommandHandlerLifecycleBlahJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_LISTENER)));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorPeopleEventJmsListener");

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
                                        .withMediaTypeWithDefaultSchema("application/vnd.structure.test-cmd+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureControllerCommandJmsListener");

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
                                        .withMediaTypeWithDefaultSchema("application/vnd.structure.test-cmd+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureControllerCommandJmsListener");

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
                                        .withMediaTypeWithDefaultSchema("application/vnd.structure.test-event+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventListenerStructureEventJmsListener");

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
                                        .withMediaTypeWithDefaultSchema("application/vnd.structure.events.test-event+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureEventJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorSomeEventJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureControllerCommandJmsListener");

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
                                        .withMediaTypeWithDefaultSchema("application/vnd.people.command1+json")
                                        .withMediaTypeWithDefaultSchema("application/vnd.people.command2+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorPeopleControllerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(startsWith("CPPNAME in")),
                        propertyValue(allOf(containsString("'people.command1'"),
                                containsString("'people.command2'"))))));
    }

    @Test
    public void shouldCreateJmsEndpointWithOnMessage() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorSomecontextControllerCommandJmsListener");

        final List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getReturnType(), CoreMatchers.equalTo(void.class));
        assertThat(method.getParameterCount(), Matchers.is(1));
        assertThat(method.getParameters()[0].getType(), CoreMatchers.equalTo(Message.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallJmsProcessorWhenOnMessageIsInvoked() throws Exception {
        generator.run(raml().withDefaultMessagingResource().build(), configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorSomecontextControllerCommandJmsListener");

        final Object object = instantiate(clazz);
        assertThat(object, is(instanceOf(MessageListener.class)));

        final MessageListener jmsListener = (MessageListener) object;
        final Message message = mock(Message.class);
        jmsListener.onMessage(message);

        final ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(jmsProcessor).process(consumerCaptor.capture(), eq(message));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope);
        consumerCaptor.getValue().accept(interceptorContext);

        verify(interceptorChainProcessor).process(interceptorContext);
    }

    @Test
    public void shouldCreateDurableTopicSubscriber() throws Exception {
        generator.run(raml()
                        .withBaseUri("message://event/listener/message/people")
                        .with(resource()
                                .withRelativeUri("/people.event")
                                .with(httpAction(POST, "application/vnd.context1.event.abc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_LISTENER)));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "PeopleEventListenerPeopleEventJmsListener");

        final ActivationConfigProperty[] activationConfig = clazz.getAnnotation(MessageDriven.class).activationConfig();
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "PeopleCommandControllerPeopleControllerCommandJmsListener");

        final ActivationConfigProperty[] activationConfig = clazz.getAnnotation(MessageDriven.class).activationConfig();
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "ContextEventProcessorStructureEventJmsListener");

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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withCustomMDBPool()));

        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "PeopleEventListenerPeoplePersonAddedJmsListener");

        final Pool poolAnnotation = clazz.getAnnotation(Pool.class);
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
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));
        final Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "PeopleEventListenerPeoplePersonAddedJmsListener");

        final Pool poolAnnotation = clazz.getAnnotation(Pool.class);
        assertThat(poolAnnotation, nullValue());

    }

    private Object instantiate(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "jmsProcessor", jmsProcessor);
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
