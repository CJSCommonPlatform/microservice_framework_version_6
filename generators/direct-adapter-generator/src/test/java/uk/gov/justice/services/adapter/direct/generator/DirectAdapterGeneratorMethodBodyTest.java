package uk.gov.justice.services.adapter.direct.generator;


import static java.util.Collections.emptyMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DirectAdapterGeneratorMethodBodyTest extends BaseGeneratorTest {

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Before
    public void setUp() throws Exception {
        generator = new DirectAdapterGenerator();
        when(interceptorChainProcessor.process(any(InterceptorContext.class))).thenReturn(Optional.of(envelope().build()));
    }


    @Test
    public void shouldPassEnvelopeToInterceptorChain() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/something")
                                .with(httpAction(GET)
                                        .withResponseTypes(
                                                "application/vnd.ctx.query.query1+json")
                                        .with(mapping()
                                                .withName("action1")
                                                .withResponseType("application/vnd.ctx.query.query1+json"))
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final SynchronousDirectAdapter adapter = (SynchronousDirectAdapter) instanceOf(compiler.compiledClassOf(BASE_PACKAGE, "QueryApiSomethingDirectAdapter"));

        final JsonEnvelope envelopePassedToAdapter = envelope().with(metadataWithRandomUUID("action1")).build();

        adapter.process(envelopePassedToAdapter);

        ArgumentCaptor<InterceptorContext> interceptorContext = ArgumentCaptor.forClass(InterceptorContext.class);

        verify(interceptorChainProcessor).process(interceptorContext.capture());

        assertThat(interceptorContext.getValue().inputEnvelope(), is(envelopePassedToAdapter));

    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionNotSupported() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/some-other-thing")
                                .with(httpAction(GET)
                                        .withResponseTypes(
                                                "application/vnd.ctx.query.query1+json",
                                                "application/vnd.ctx.query.query2+json")
                                        .with(mapping()
                                                .withName("action1")
                                                .withResponseType("application/vnd.ctx.query.query1+json"))
                                        .with(mapping()
                                                .withName("action2")
                                                .withResponseType("application/vnd.ctx.query.query2+json"))
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final SynchronousDirectAdapter adapter = (SynchronousDirectAdapter) instanceOf(compiler.compiledClassOf(BASE_PACKAGE, "QueryApiSomeOtherThingDirectAdapter"));

        adapter.process(envelope().with(metadataWithRandomUUID("action3")).build());

    }

    private Object instanceOf(final Class<?> adapterClass) throws InstantiationException, IllegalAccessException {
        final Object resourceObject = adapterClass.newInstance();
        setField(resourceObject, "interceptorChainProcessor", interceptorChainProcessor);
        return resourceObject;
    }
}
