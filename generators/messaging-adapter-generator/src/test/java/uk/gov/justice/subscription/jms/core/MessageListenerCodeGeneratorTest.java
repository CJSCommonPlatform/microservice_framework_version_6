package uk.gov.justice.subscription.jms.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.domain.EventBuilder.event;
import static uk.gov.justice.domain.EventsourceBuilder.eventsource;
import static uk.gov.justice.domain.LocationBuilder.location;
import static uk.gov.justice.domain.SubscriptionBuilder.subscription;
import static uk.gov.justice.domain.SubscriptionDescriptorBuilder.subscriptionDescriptor;

import uk.gov.justice.domain.subscriptiondescriptor.Event;
import uk.gov.justice.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;
import uk.gov.justice.raml.jms.config.GeneratorPropertiesFactory;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;

import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageListenerCodeGeneratorTest  {

    @InjectMocks
    private MessageListenerCodeGenerator messageListenerCodeGenerator;


    @Test
    public void shouldGenerateMDBForTopic() throws Exception {
        final String basePackageName = "uk.gov.moj.base.package.name";
        final String serviceName = "my-context";
        final String componentName = "EVENT_LISTENER";
        final String jmsUri = "jms:topic:my-context.handler.command";

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final Event event_2 = event()
                .withName("my-context.events.something-else-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-else-happened.json")
                .build();


        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEvent(event_2)
                .withEventsource(eventsource()
                        .withName("eventsource")
                        .withLocation(location()
                                .withJmsUri(jmsUri)
                                .withRestUri("http://localhost:8080/example/event-source-api/rest")
                                .build())
                        .build())
                .build();
        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final ClassNameFactory classNameFactory =
                new ClassNameFactory(basePackageName, serviceName, componentName, jmsUri);

        GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withCustomMDBPool();

        final TypeSpec typeSpec = messageListenerCodeGenerator.generate(subscriptionDescriptor, subscription, (CommonGeneratorProperties) generatorProperties, classNameFactory);

        assertThat(typeSpec.toString(), is("@uk.gov.justice.services.core.annotation.Adapter(\"EVENT_LISTENER\")\n" +
                "@javax.ejb.MessageDriven(\n" +
                "    activationConfig = {\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"javax.jms.Topic\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"destinationLookup\", propertyValue = \"my-context.handler.command\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"shareSubscriptions\", propertyValue = \"true\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"subscriptionDurability\", propertyValue = \"Durable\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"clientId\", propertyValue = \"my-context.event.listener\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"subscriptionName\", propertyValue = \"my-context.event.listener.my-context.handler.command\")\n" +
                "    }\n" +
                ")\n" +
                "@javax.interceptor.Interceptors({\n" +
                "    uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataInterceptor.class,\n" +
                "    uk.gov.moj.base.package.name.MyContextEventListenerMyContextHandlerCommandEventValidationInterceptor.class\n" +
                "})\n" +
                "@org.jboss.ejb3.annotation.Pool(\"my-context-handler-command-event-listener-pool\")\n" +
                "public class MyContextEventListenerMyContextHandlerCommandJmsListener implements javax.jms.MessageListener {\n" +
                "  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextEventListenerMyContextHandlerCommandJmsListener.class);\n" +
                "\n" +
                "  @javax.inject.Inject\n" +
                "  uk.gov.justice.services.core.interceptor.InterceptorChainProcessor interceptorChainProcessor;\n" +
                "\n" +
                "  @javax.inject.Inject\n" +
                "  uk.gov.justice.services.adapter.messaging.JmsProcessor jmsProcessor;\n" +
                "\n" +
                "  @java.lang.Override\n" +
                "  public void onMessage(javax.jms.Message message) {\n" +
                "    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> \"Received JMS message\");\n" +
                "    jmsProcessor.process(interceptorChainProcessor::process, message);\n" +
                "  }\n" +
                "}\n"));
    }

    @Test
    public void shouldGenerateMDBForCommandHandlerQueue() throws Exception {
        final String basePackageName = "uk.gov.moj.base.package.name";
        final String serviceName = "my-context";
        final String componentName = "COMMAND_HANDLER";
        final String jmsUri = "jms:topic:my-context.handler.command";

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final Event event_2 = event()
                .withName("my-context.events.something-else-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-else-happened.json")
                .build();


        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEvent(event_2)
                .withEventsource(eventsource()
                        .withName("eventsource")
                        .withLocation(location()
                                .withJmsUri(jmsUri)
                                .withRestUri("http://localhost:8080/example/event-source-api/rest")
                                .build())
                        .build())
                .build();
        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final ClassNameFactory classNameFactory =
                new ClassNameFactory(basePackageName, serviceName, componentName, jmsUri);

        GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withServiceComponentOf(componentName);

        final TypeSpec typeSpec = messageListenerCodeGenerator.generate(subscriptionDescriptor, subscription, (CommonGeneratorProperties) generatorProperties, classNameFactory);

        assertThat(typeSpec.toString(), is("@uk.gov.justice.services.core.annotation.Adapter(\"COMMAND_HANDLER\")\n" +
                "@javax.ejb.MessageDriven(\n" +
                "    activationConfig = {\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"javax.jms.Queue\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"destinationLookup\", propertyValue = \"my-context.handler.command\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"shareSubscriptions\", propertyValue = \"true\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"messageSelector\", propertyValue = \"CPPNAME in('my-context.events.something-happened','my-context.events.something-else-happened')\")\n" +
                "    }\n" +
                ")\n" +
                "@javax.interceptor.Interceptors({\n" +
                "    uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataInterceptor.class,\n" +
                "    uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class\n" +
                "})\n" +
                "public class MyContextCommandHandlerMyContextHandlerCommandJmsListener implements javax.jms.MessageListener {\n" +
                "  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextCommandHandlerMyContextHandlerCommandJmsListener.class);\n" +
                "\n" +
                "  @javax.inject.Inject\n" +
                "  uk.gov.justice.services.core.interceptor.InterceptorChainProcessor interceptorChainProcessor;\n" +
                "\n" +
                "  @javax.inject.Inject\n" +
                "  uk.gov.justice.services.adapter.messaging.JmsProcessor jmsProcessor;\n" +
                "\n" +
                "  @java.lang.Override\n" +
                "  public void onMessage(javax.jms.Message message) {\n" +
                "    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> \"Received JMS message\");\n" +
                "    jmsProcessor.process(interceptorChainProcessor::process, message);\n" +
                "  }\n" +
                "}\n"));
    }

    @Test
    public void shouldGenerateMDBForQueue() throws Exception {
        final String basePackageName = "uk.gov.moj.base.package.name";
        final String serviceName = "my-context";
        final String componentName = "COMMAND_API";
        final String jmsUri = "jms:queue:my-context.handler.command";

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final Event event_2 = event()
                .withName("my-context.events.something-else-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-else-happened.json")
                .build();


        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEvent(event_2)
                .withEventsource(eventsource()
                        .withName("eventsource")
                        .withLocation(location()
                                .withJmsUri(jmsUri)
                                .withRestUri("http://localhost:8080/example/event-source-api/rest")
                                .build())
                        .build())
                .build();
        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final ClassNameFactory classNameFactory =
                new ClassNameFactory(basePackageName, serviceName, componentName, jmsUri);

        GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withServiceComponentOf("COMMAND_API");

        final TypeSpec typeSpec = messageListenerCodeGenerator.generate(subscriptionDescriptor, subscription, (CommonGeneratorProperties) generatorProperties, classNameFactory);

        assertThat(typeSpec.toString(), is("@uk.gov.justice.services.core.annotation.Adapter(\"COMMAND_API\")\n" +
                "@javax.ejb.MessageDriven(\n" +
                "    activationConfig = {\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"javax.jms.Queue\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"destinationLookup\", propertyValue = \"my-context.handler.command\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"shareSubscriptions\", propertyValue = \"true\"),\n" +
                "        @javax.ejb.ActivationConfigProperty(propertyName = \"messageSelector\", propertyValue = \"CPPNAME in('my-context.events.something-happened','my-context.events.something-else-happened')\")\n" +
                "    }\n" +
                ")\n" +
                "@javax.interceptor.Interceptors({\n" +
                "    uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataInterceptor.class,\n" +
                "    uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class\n" +
                "})\n" +
                "public class MyContextCommandApiMyContextHandlerCommandJmsListener implements javax.jms.MessageListener {\n" +
                "  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextCommandApiMyContextHandlerCommandJmsListener.class);\n" +
                "\n" +
                "  @javax.inject.Inject\n" +
                "  uk.gov.justice.services.core.interceptor.InterceptorChainProcessor interceptorChainProcessor;\n" +
                "\n" +
                "  @javax.inject.Inject\n" +
                "  uk.gov.justice.services.adapter.messaging.JmsProcessor jmsProcessor;\n" +
                "\n" +
                "  @java.lang.Override\n" +
                "  public void onMessage(javax.jms.Message message) {\n" +
                "    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> \"Received JMS message\");\n" +
                "    jmsProcessor.process(interceptorChainProcessor::process, message);\n" +
                "  }\n" +
                "}\n"));
    }
}
