package uk.gov.justice.services.clients.messaging.generator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.config.GeneratorPropertiesFactory.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessagingClientGenerator_MethodBodyTest {

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Mock
    private JmsEnvelopeSender sender;

    private final MessagingClientGenerator generator = new MessagingClientGenerator();

    private JavaCompilerUtil compiler;

    @Before
    public void before() {
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldSendEnvelopeToDestination() throws Exception {
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .withRelativeUri("/cakeshop.controller.command")
                                .withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_CONTROLLER")));

        final Class<?> generatedClass = compiler.compiledClassOf(BASE_PACKAGE, "RemoteCommandController2EventProcessorMessageContextCakeshopControllerCommand");
        final Object instance = instanceOf(generatedClass);
        setField(instance, "traceLogger", mock(TraceLogger.class));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final Method method = firstMethodOf(generatedClass).get();
        method.invoke(instance, envelope);

        verify(sender).send(envelope, "cakeshop.controller.command");

    }

    private Object instanceOf(final Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "sender", sender);
        return resourceObject;
    }
}
