package uk.gov.justice.subscription.jms.core;

import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.allOf;
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
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.justice.subscription.domain.builders.EventBuilder.event;
import static uk.gov.justice.subscription.domain.builders.EventSourceBuilder.eventsource;
import static uk.gov.justice.subscription.domain.builders.LocationBuilder.location;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorBuilder.subscriptionDescriptor;

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
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;
import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.jms.parser.SubscriptionWrapper;

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
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionJmsEndpointGeneratorTest {
    private static final String BASE_PACKAGE = "uk.test";
    private static final String BASE_PACKAGE_FOLDER = "/uk/test";
    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    JmsProcessor jmsProcessor;

    @Mock
    InterceptorChainProcessor interceptorChainProcessor;

    private GeneratorProperties generatorProperties;
    private JavaCompilerUtil compiler;
    private Generator<SubscriptionWrapper> generator;
    private final String serviceName = "context";
    private final String componentName = "EVENT_PROCESSOR";


    @Before
    public void setup() throws Exception {
        generator = new JmsEndpointGenerationObjects().subscriptionJmsEndpointGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
        generatorProperties = new GeneratorPropertiesFactory().withDefaultServiceComponent();
    }

    @Test
    public void shouldCreateJmsClass() throws Exception {
        final SubscriptionWrapper subscriptionWrapper = setUpMessageSubscription("jms:topic:structure.controller.command", "my-context.events.something-happened", serviceName, componentName);
        generator.run(subscriptionWrapper,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        assertThat(asList(packageDir.listFiles()),
                hasItem(hasProperty("name", equalTo("ContextEventProcessorStructureControllerCommandJmsListener.java"))));
    }

    @Test
    public void shouldCreateClassContainsHyphens() throws Exception {
        final SubscriptionWrapper subscriptionWrapper = setUpMessageSubscription("jms:topic:structure.event", "some event", "context-with-hyphens", componentName);

        generator.run(subscriptionWrapper,
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> compiledClass = compiler.compiledClassOf("uk.somepackage", "ContextWithHyphensEventProcessorStructureEventJmsListener");
        assertThat(compiledClass.getName(), is("uk.somepackage.ContextWithHyphensEventProcessorStructureEventJmsListener"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateMultipleJmsClasses() throws Exception {

        String structureJmsUri = "jms:topic:structure.controller.command";
        String peopleJmsUri = "jms:topic:people.controller.command";

        final Event event = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final EventSource eventsource = eventsource()
                .withName("eventSource")
                .withLocation(location()
                        .withJmsUri(structureJmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final EventSource eventsource2 = eventsource()
                .withName("eventSource2")
                .withLocation(location()
                        .withJmsUri(peopleJmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event)
                .withEventSourceName("eventSource")
                .build();

        final Subscription subscription2 = subscription()
                .withName("subscription2")
                .withEvent(event)
                .withEventSourceName("eventSource2")
                .build();

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .withSubscription(subscription2)
                .build();

        final SubscriptionWrapper subscriptionWrapper = new SubscriptionWrapper(subscriptionDescriptor, asList(eventsource, eventsource2));
        generator.run(subscriptionWrapper,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        final File[] a = packageDir.listFiles();
        assertThat(asList(a),
                hasItems(hasProperty("name", equalTo("ContextEventProcessorPeopleControllerCommandJmsListener.java")),
                        hasProperty("name", equalTo("ContextEventProcessorStructureControllerCommandJmsListener.java"))
                ));
    }

    @Test
    public void shouldOverwriteJmsClass() throws Exception {
        final String path = outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER;
        final File packageDir = new File(path);
        packageDir.mkdirs();
        write(Paths.get(path + "/StructureControllerCommandJmsListener.java"),
                Collections.singletonList("Old file content"));
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "my-context.events.something-happened", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final List<String> lines = Files.readAllLines(Paths.get(path + "/ContextEventProcessorStructureControllerCommandJmsListener.java"));
        assertThat(lines.get(0), not(containsString("Old file content")));
    }

    @Test
    public void shouldCreateJmsEndpointNamedAfterTopic() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "my-context.events.something-happened", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf("uk.somepackage", "ContextEventProcessorStructureControllerCommandJmsListener");
        assertThat(clazz.getName(), is("uk.somepackage.ContextEventProcessorStructureControllerCommandJmsListener"));
    }

    @Test
    public void shouldCreateLoggerConstant() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "my-context.events.something-happened", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> resourceClass = compiler.compiledClassOf("uk.somepackage", "ContextEventProcessorStructureControllerCommandJmsListener");

        final Field logger = resourceClass.getDeclaredField("LOGGER");
        assertThat(logger, CoreMatchers.not(nullValue()));
        assertThat(logger.getType(), CoreMatchers.equalTo(Logger.class));
        assertThat(Modifier.isPrivate(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isStatic(logger.getModifiers()), Matchers.is(true));
        assertThat(Modifier.isFinal(logger.getModifiers()), Matchers.is(true));
    }

    @Test
    public void shouldCreateJmsEndpointInADifferentPackage() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "my-context.events.something-happened", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage("uk.package2", outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf("uk.package2", "ContextEventProcessorStructureControllerCommandJmsListener");
        assertThat(clazz.getName(), is("uk.package2.ContextEventProcessorStructureControllerCommandJmsListener"));
    }

    @Test
    public void shouldCreateJmsEventProcessorNamedAfterDestinationNameAndContextName() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.event", "some event", "context", componentName);


        generator.run(subscriptionDescriptor,
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties));

        final Class<?> compiledClass = compiler.compiledClassOf("uk.somepackage", "ContextEventProcessorStructureEventJmsListener");
        assertThat(compiledClass.getName(), is("uk.somepackage.ContextEventProcessorStructureEventJmsListener"));
    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithCommandHandlerAdapter() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.some.queue", "people.abc", "abc", "COMMAND_HANDLER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(COMMAND_HANDLER)));
        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "AbcCommandHandlerPeopleSomeQueueJmsListener");
        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(COMMAND_HANDLER));
    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithControllerCommandAdapter() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.some.query", "people.abc", "abc", "COMMAND_CONTROLLER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(COMMAND_CONTROLLER)));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "AbcCommandControllerPeopleSomeQueryJmsListener");
        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(COMMAND_CONTROLLER));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithEventListenerAdapter() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.event", "people.abc", "people", "EVENT_LISTENER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_LISTENER)));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeopleEventJmsListener");
        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(Component.EVENT_LISTENER));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithCustomEventListenerAdapter() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.event", "people.abc", "custom", "CUSTOM_EVENT_LISTENER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf("CUSTOM_EVENT_LISTENER")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "CustomCustomEventListenerPeopleEventJmsListener");
        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);

        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is("CUSTOM_EVENT_LISTENER"));

        final Class<?> customEventFilterInterceptor = compiler.compiledClassOf(BASE_PACKAGE, "CustomCustomEventListenerPeopleEventEventFilterInterceptor");
        final Field eventFilter = customEventFilterInterceptor.getDeclaredField("eventFilter");
        final Class<?> customEventFilterClass = eventFilter.getType();

        assertThat(customEventFilterClass.getName(), is("uk.test.CustomCustomEventListenerPeopleEventEventFilter"));
    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithEventProcessorAdapter() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.event", "people.abc", "people", componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_PROCESSOR)));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventProcessorPeopleEventJmsListener");
        final Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);

        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(Component.EVENT_PROCESSOR));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithInterceptors() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.handler.command", "people.abc", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));
        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleHandlerCommandJmsListener");
        final Interceptors interceptorsAnnotation = clazz.getAnnotation(Interceptors.class);

        assertThat(interceptorsAnnotation, not(nullValue()));
        assertThat(interceptorsAnnotation.value(), hasItemInArray(JsonSchemaValidationInterceptor.class));
        assertThat(interceptorsAnnotation.value(), hasItemInArray(JmsLoggerMetadataInterceptor.class));
    }

    @Test
    public void shouldCreateJmsEndpointImplementingMessageListener() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:somecontext.controller.command", "somecontext.command1", serviceName, componentName);

        generator.run(subscriptionDescriptor, configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");

        assertThat(clazz.getInterfaces().length, equalTo(1));
        assertThat(clazz.getInterfaces()[0], equalTo(MessageListener.class));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedInterceptorChainProcessorProperty() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:somecontext.controller.command", "somecontext.command1", serviceName, componentName);

        generator.run(subscriptionDescriptor, configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
        final Field chainProcessField = clazz.getDeclaredField(INTERCEPTOR_CHAIN_PROCESSOR);

        assertThat(chainProcessField, not(nullValue()));
        assertThat(chainProcessField.getType(), CoreMatchers.equalTo((InterceptorChainProcessor.class)));
        assertThat(chainProcessField.getAnnotations(), arrayWithSize(1));
        assertThat(chainProcessField.getAnnotation(Inject.class), not(nullValue()));
    }

    @Test
    public void shouldCreateJmsEndpointWithAnnotatedJmsProcessorProperty() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:somecontext.controller.command", "somecontext.command1", serviceName, componentName);

        generator.run(subscriptionDescriptor, configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
        final Field jmsProcessorField = clazz.getDeclaredField("jmsProcessor");

        assertThat(jmsProcessorField, not(nullValue()));
        assertThat(jmsProcessorField.getType(), CoreMatchers.equalTo((JmsProcessor.class)));
        assertThat(jmsProcessorField.getAnnotations(), arrayWithSize(1));
        assertThat(jmsProcessorField.getAnnotation(Inject.class), not(nullValue()));
    }

    @Test
    public void shouldCreateAnnotatedCommandControllerEndpointWithDestinationLookupProperty() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.controller.command", "people.abc", serviceName, "EVENT_PROCESSOR");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleControllerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("people.controller.command")))));
    }

    @Test
    public void shouldCreateAnnotatedCommandControllerEndpointWithDestinationLookupProperty2() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "my-context.events.something-happened", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("structure.controller.command")))));
    }

    @Test
    public void shouldCreateAnnotatedCommandHandlerEndpointWithDestinationLookupProperty3() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.handler.command", "people.abc", serviceName, componentName);


        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureHandlerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("structure.handler.command")))));
    }

    @Test
    public void shouldCreateAnnotatedEventListenerEndpointWithDestinationLookupProperty3() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.event", "structure.abc", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureEventJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationLookup")),
                        propertyValue(equalTo("structure.event")))));
    }

    @Test
    public void shouldCreateAnnotatedControllerCommandEndpointWithQueueAsDestinationType() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:queue:structure.something", "structure.abc", "people", "COMMAND_CONTROLLER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleCommandControllerStructureSomethingJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationType")),
                        propertyValue(equalTo("javax.jms.Queue")))));
    }

    @Test
    public void shouldCreateAnnotatedCommandHandlerEndpointWithQueueAsDestinationType() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:lifecycle.blah", "lifecycle.abc", "aaa", "COMMAND_HANDLER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "AaaCommandHandlerLifecycleBlahJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationType")),
                        propertyValue(equalTo("javax.jms.Queue")))));
    }

    @Test
    public void shouldCreateAnnotatedEventListenerEndpointWithQueueAsDestinationType() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.event", "people.abc", serviceName, "EVENT_PROCESSOR");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_LISTENER)));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleEventJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("destinationType")),
                        propertyValue(equalTo("javax.jms.Topic")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithMessageSelectorContainingOneCommandWithAPost() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "structure.test-cmd", serviceName, "EVENT_PROCESSOR");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-cmd')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointFromMediaTypeWithoutPillar() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "structure.test-cmd", serviceName, "EVENT_PROCESSOR");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-cmd')")))));
    }

    @Test
    public void shouldNotAddMessageSelectorForEventListener() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.event", "structure.test-event", serviceName, "EVENT_LISTENER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventListenerStructureEventJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                not(hasItemInArray(propertyName(equalTo("messageSelector")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithMessageSelectorContainingOneEvent_PluralPillarName() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.event", "structure.events.test-event", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureEventJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.events.test-event')")))));
    }

    @Test
    public void shouldOnlyCreateMessageSelectorForPostActionAndIgnoreAllOtherActions() throws Exception {

        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.controller.command", "structure.test-cmd1", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureControllerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(equalTo("CPPNAME in('structure.test-cmd1')")))));
    }

    @Test
    public void shouldCreateAnnotatedJmsEndpointWithMessageSelectorContainingTwoCommand() throws Exception {
        final String jmsUri = "jms:topic:people.controller.command";

        final Event event = event()
                .withName("people.command1")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/people.command1.json")
                .build();

        final Event event2 = event()
                .withName("people.command2")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/people.command2.json")
                .build();

        final EventSource eventsource = eventsource()
                .withName("eventSource")
                .withLocation(location()
                        .withJmsUri(jmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event)
                .withEvent(event2)
                .withEventSourceName("eventSource")
                .build();

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final SubscriptionWrapper subscriptionWrapper = new SubscriptionWrapper(subscriptionDescriptor, asList(eventsource));

        generator.run(subscriptionWrapper,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorPeopleControllerCommandJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("messageSelector")),
                        propertyValue(startsWith("CPPNAME in")),
                        propertyValue(allOf(containsString("'people.command1'"),
                                containsString("'people.command2'"))))));
    }

    @Test
    public void shouldCreateJmsEndpointWithOnMessage() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:somecontext.controller.command", "somecontext.command1", serviceName, componentName);
        generator.run(subscriptionDescriptor, configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");

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
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:somecontext.controller.command", "somecontext.command1", serviceName, componentName);

        generator.run(subscriptionDescriptor, configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorSomecontextControllerCommandJmsListener");
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
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.event", "context1.event.abc", "people", "EVENT_LISTENER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_LISTENER)));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeopleEventJmsListener");
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
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.controller.command", "people.event.abc", "people", "COMMAND_CONTROLLER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleCommandControllerPeopleControllerCommandJmsListener");
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
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:structure.event", "structure.abc", serviceName, componentName);

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "ContextEventProcessorStructureEventJmsListener");

        assertThat(clazz.getAnnotation(MessageDriven.class), is(notNullValue()));
        assertThat(clazz.getAnnotation(MessageDriven.class).activationConfig(),
                hasItemInArray(allOf(propertyName(equalTo("shareSubscriptions")),
                        propertyValue(equalTo("true")))));
    }


    @Test
    public void shouldCreateJmsEndpointAnnotatedWithPoolConfiguration() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.person-added", "people.abc", "people", "EVENT_LISTENER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new GeneratorPropertiesFactory().withCustomMDBPool()));
        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeoplePersonAddedJmsListener");
        final Pool poolAnnotation = clazz.getAnnotation(Pool.class);

        assertThat(poolAnnotation, not(nullValue()));
        assertThat(poolAnnotation.value(), is("people-person-added-event-listener-pool"));
    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithoutPoolConfiguration() throws Exception {
        final SubscriptionWrapper subscriptionDescriptor = setUpMessageSubscription("jms:topic:people.person-added", "people.abc", "people", "EVENT_LISTENER");

        generator.run(subscriptionDescriptor,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));
        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "PeopleEventListenerPeoplePersonAddedJmsListener");
        Pool poolAnnotation = clazz.getAnnotation(Pool.class);

        assertThat(poolAnnotation, nullValue());
    }

    private Object instantiate(final Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "jmsProcessor", jmsProcessor);
        setField(resourceObject, INTERCEPTOR_CHAIN_PROCESSOR, interceptorChainProcessor);
        return resourceObject;
    }

    private FeatureMatcher<ActivationConfigProperty, String> propertyName(Matcher<String> matcher) {
        return new FeatureMatcher<ActivationConfigProperty, String>(matcher, "propertyName", "propertyName") {
            @Override
            protected String featureValueOf(final ActivationConfigProperty actual) {
                return actual.propertyName();
            }
        };
    }

    private FeatureMatcher<ActivationConfigProperty, String> propertyValue(final Matcher<String> matcher) {
        return new FeatureMatcher<ActivationConfigProperty, String>(matcher, "propertyValue", "propertyValue") {
            @Override
            protected String featureValueOf(ActivationConfigProperty actual) {
                return actual.propertyValue();
            }
        };
    }

    private SubscriptionWrapper setUpMessageSubscription(final String jmsUri, final String eventName, final String serviceName, final String componentName) {
        final Event event = event()
                .withName(eventName)
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/" + eventName + ".json")
                .build();

        final EventSource eventsource = eventsource()
                .withName("eventSource")
                .withLocation(location()
                        .withJmsUri(jmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event)
                .withEventSourceName("eventSource")
                .build();

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        return new SubscriptionWrapper(subscriptionDescriptor, asList(eventsource));
    }
}
