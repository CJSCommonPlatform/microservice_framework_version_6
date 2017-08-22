package uk.gov.justice.services.clients.direct.generator;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultGetResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapterCache;
import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

public class DirectClientGeneratorCodeStructureTest extends BaseGeneratorTest {

    @Before
    public void setUp() throws Exception {
        generator = new DirectClientGenerator();
    }

    @Test
    public void shouldGenerateClassWithAnnotations() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/service")
                        .with(defaultGetResource())
                        .build(),
                configurationWithBasePackage("uk.somepackage", outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> generatedClass = compiler.compiledClassOf("uk.somepackage", "DirectSomeComponent2QueryApiServiceClient");
        assertThat(generatedClass.getCanonicalName(), is("uk.somepackage.DirectSomeComponent2QueryApiServiceClient"));
        assertThat(generatedClass.getAnnotation(Direct.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(Direct.class).target(), is("QUERY_API"));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("SOME_COMPONENT"));

    }

    @Test
    public void shouldGenerateClassWithInjectedAdapterCache() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/service")
                        .with(defaultGetResource())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "DirectSomeComponent2QueryApiServiceClient");
        final Field adapterField = clazz.getDeclaredField("adapterCache");
        assertThat(adapterField.getAnnotation(Inject.class), not(nullValue()));
        assertThat(adapterField.getGenericType(), equalTo(SynchronousDirectAdapterCache.class));
    }


    @Test
    public void shouldGenerateTwoMethodsAnnotatedWithHandlesAnnotationForGET() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/service")
                        .with(resource()
                                .with(httpAction(GET)
                                        .withResponseTypes(
                                                "application/vnd.ctx.query2+json",
                                                "application/vnd.ctx.query1+json")
                                        .with(mapping()
                                                .withName("actionABC")
                                                .withResponseType("application/vnd.ctx.query1+json"))
                                        .with(mapping()
                                                .withName("actionBCD")
                                                .withResponseType("application/vnd.ctx.query2+json"))
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));


        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "DirectSomeComponent2QueryApiServiceClient");
        final List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(2));

        final Method method1 = methods.get(0);
        final Handles handlesAnnotation1 = method1.getAnnotation(Handles.class);
        assertThat(handlesAnnotation1, not(nullValue()));
        assertThat(handlesAnnotation1.value(), is("actionABC"));


        final Method method2 = methods.get(1);
        final Handles handlesAnnotation2 = method2.getAnnotation(Handles.class);
        assertThat(handlesAnnotation2, not(nullValue()));
        assertThat(handlesAnnotation2.value(), is("actionBCD"));

    }

    @Test
    public void shouldGenerateMethodAcceptingAndReturningEnvelope() throws MalformedURLException {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/service")
                        .with(defaultGetResource())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));

        final Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "DirectSomeComponent2QueryApiServiceClient");
        final Method method = firstMethodOf(clazz);
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo((JsonEnvelope.class)));
        assertThat(method.getReturnType(), equalTo((JsonEnvelope.class)));
    }

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotSet() {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("serviceComponent generator property not set in the plugin config");

        generator.run(
                restRamlWithDefaults()
                        .withDefaultGetResource()
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldIgnoreNonVendorSpecificMediaTypes() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/query/api/service")
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(GET)
                                        .withResponseTypes("text/csv")
                                        .with(mapping()
                                                .withName("action1")
                                                .withResponseType("text/csv"))
                                ))
                        .with(resource("/pathbcd/{anId}")
                                .with(httpAction()
                                        .withHttpActionType(GET)
                                        .withResponseTypes("application/abc+json")
                                        .with(mapping()
                                                .withName("action2")
                                                .withResponseType("application/abc+json"))
                                ))

                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("SOME_COMPONENT")));


        assertThat(methodsOf(compiler.compiledClassOf(BASE_PACKAGE, "DirectSomeComponent2QueryApiServiceClient")), hasSize(0));

    }


}