package uk.gov.justice.services.adapter.direct.generator;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.services.adapter.direct.DirectAdapterProcessor;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

public class DirectAdapterGeneratorCodeStructureTest extends BaseGeneratorTest {

    @Before
    public void setUp() throws Exception {
        generator = new DirectAdapterGenerator();

    }

    @Test
    public void shouldGenerateAdapterClassImplementingInterface() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/something")
                                .with(defaultGetAction()))
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, emptyMap()));

        final Class<?> adapterClass = compiler.compiledClassOf("uk.somepackage", "QueryApiSomethingDirectAdapter");
        assertThat(adapterClass.getName(), is("uk.somepackage.QueryApiSomethingDirectAdapter"));
        assertThat(adapterClass.getInterfaces(), arrayContaining(SynchronousDirectAdapter.class));

    }

    @Test
    public void shouldGenerateClassWithDirectAdapterAnnotation() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/some-other-thing")
                                .with(httpAction(GET)
                                        .withResponseTypes(
                                                "application/vnd.ctx.query.query1+json",
                                                "application/vnd.ctx.query.query2+json")
                                        .with(mapping()
                                                .withName("actionABC")
                                                .withResponseType("application/vnd.ctx.query.query1+json"))
                                        .with(mapping()
                                                .withName("actionBCD")
                                                .withResponseType("application/vnd.ctx.query.query2+json"))
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> adapterClass = compiler.compiledClassOf(BASE_PACKAGE, "QueryApiSomeOtherThingDirectAdapter");
        final DirectAdapter adapterAnnotation = adapterClass.getAnnotation(DirectAdapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.component(), is("QUERY_API"));
        assertThat(adapterAnnotation.actions(), arrayContainingInAnyOrder("actionABC", "actionBCD"));


    }

    @Test
    public void shouldGenerateAdapterClassWithOneMethod() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/something")
                                .with(defaultGetAction()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> adapterClass = compiler.compiledClassOf(BASE_PACKAGE, "QueryApiSomethingDirectAdapter");

        final List<Method> methods = methodsOf(adapterClass);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getName(), is("process"));
        assertThat(method.getReturnType(), equalTo(JsonEnvelope.class));
        assertThat(method.getParameterTypes(), arrayWithSize(1));
        assertThat(method.getParameterTypes()[0], equalTo(JsonEnvelope.class));

    }

    @Test
    public void shouldGenerateClassWithInjectedInterceptorChainProcessorField() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/abc")
                                .with(defaultGetAction()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> adapterClass = compiler.compiledClassOf(BASE_PACKAGE, "QueryApiAbcDirectAdapter");

        final Field interceptorChainProcessorField = adapterClass.getDeclaredField("interceptorChainProcessor");
        assertThat(interceptorChainProcessorField, not(nullValue()));
        assertThat(interceptorChainProcessorField.getType(), equalTo(InterceptorChainProcessor.class));
        assertThat(interceptorChainProcessorField.getAnnotation(Inject.class), not(nullValue()));

    }

    @Test
    public void shouldGenerateClassWithAdapterProcessorField() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/abc")
                                .with(defaultGetAction()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> adapterClass = compiler.compiledClassOf(BASE_PACKAGE, "QueryApiAbcDirectAdapter");

        final Field directAdapterProcessor = adapterClass.getDeclaredField("directAdapterProcessor");
        assertThat(directAdapterProcessor, not(nullValue()));
        assertThat(directAdapterProcessor.getType(), equalTo(DirectAdapterProcessor.class));
        assertThat(directAdapterProcessor.getAnnotation(Inject.class), nullValue());

    }
}
