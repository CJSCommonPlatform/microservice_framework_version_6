package uk.gov.justice.services.clients.direct.generator;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultGetResource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DirectClientGeneratorMethodBodyTest extends BaseGeneratorTest {

    @Mock
    private SynchronousDirectAdapter adapter;

    @Before
    public void setUp() throws Exception {
        generator = new DirectClientGenerator();
    }


    @Test
    public void shouldPassEnvelopeToAdapter() throws Exception {

        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/service")
                        .with(defaultGetResource())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

        final Class<?> generatedClientClass = compiler.compiledClassOf(BASE_PACKAGE, "DirectQueryApiServiceClient");
        final JsonEnvelope envelopePassedToClient = envelope().build();

        invokeFirstMethod(generatedClientClass, envelopePassedToClient);

        verify(adapter).process(envelopePassedToClient);
    }

    @Test
    public void shouldReturnEnvelopeReturnedByAdapter() throws Exception {

        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/service")
                        .with(defaultGetResource())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));
        final Class<?> generatedClientClass = compiler.compiledClassOf(BASE_PACKAGE, "DirectQueryApiServiceClient");

        final JsonEnvelope envelopeReturnedByAdapter = envelope().build();
        when(adapter.process(any(JsonEnvelope.class))).thenReturn(envelopeReturnedByAdapter);

        final Object result = invokeFirstMethod(generatedClientClass, envelope().build());
        assertThat(result, is(envelopeReturnedByAdapter));
    }

    private Object invokeFirstMethod(Class<?> generatedClass, JsonEnvelope envelope) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        final Object directClient = instanceOf(generatedClass);

        final Method method = firstMethodOf(generatedClass);
        return method.invoke(directClient, envelope);
    }

    private Object instanceOf(final Class<?> directClientClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = directClientClass.newInstance();
        setField(resourceObject, "adapter", adapter);
        setField(resourceObject, "traceLogger", new DefaultTraceLogger());
        return resourceObject;
    }

}
