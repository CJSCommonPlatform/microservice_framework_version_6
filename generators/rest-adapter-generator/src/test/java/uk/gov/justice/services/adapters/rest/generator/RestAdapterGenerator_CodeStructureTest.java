package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.nio.file.Paths.get;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.QueryParamBuilder.queryParam;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultGetResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultPatchResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultPostResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultPutResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.rest.BasicActionMapper;
import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.rest.annotation.PATCH;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.slf4j.Logger;

public class RestAdapterGenerator_CodeStructureTest extends BaseRestAdapterGeneratorTest {

    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";

    @Test
    public void shouldGenerateAnnotatedResourceInterface() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("some/path"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        assertThat(interfaceClass.isInterface(), is(true));
        assertThat(interfaceClass.getAnnotation(Path.class), not(nullValue()));
        assertThat(interfaceClass.getAnnotation(Path.class).value(), is("some/path"));
    }

    @Test
    public void shouldGenerateInterfaceInSpecifiedPackage() throws Exception {
        String basePackageName = "uk.gov.test1";

        java.nio.file.Path outputPath = get(outputFolder.getRoot().getAbsolutePath());
        GeneratorConfig config = new GeneratorConfig(outputPath, outputPath, basePackageName, emptyMap(), Collections.singletonList(outputPath.getParent()));
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource())
                        .build(),
                config);

        Class<?> interfaceClass = compiler.compiledInterfaceOf(basePackageName);

        assertThat(interfaceClass.getPackage().getName(), is(basePackageName + ".resource"));

    }

    @Test
    public void shouldGenerateResourceInterfaceWithOnePOSTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(POST, "application/vnd.default+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(POST.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.default+json"}));
    }

    @Test
    public void shouldGenerateResourceInterfaceWithTwoPOSTMethods() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{p1}")
                                .with(httpAction(POST, "application/vnd.ctx.command.cmd-a+json", "application/vnd.ctx.command.cmd-b+json")
                                        .with(mapping().withName("blah1").withRequestType("application/vnd.ctx.command.cmd-a+json"))
                                        .with(mapping().withName("blah2").withRequestType("application/vnd.ctx.command.cmd-b+json")))

                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(2));
        Method method1 = methodWithConsumesAnnotationOf(methods, "application/vnd.ctx.command.cmd-a+json");

        assertThat(method1.getReturnType(), equalTo(Response.class));
        assertThat(method1.getAnnotation(POST.class), not(nullValue()));
        assertThat(method1.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method1.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.ctx.command.cmd-a+json"}));

        Method method2 = methodWithConsumesAnnotationOf(methods, "application/vnd.ctx.command.cmd-b+json");
        assertThat(method2.getReturnType(), equalTo(Response.class));
        assertThat(method2.getAnnotation(POST.class), not(nullValue()));
        assertThat(method2.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method2.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.ctx.command.cmd-b+json"}));

    }

    @Test
    public void shouldGenerateResourceInterfaceWithOnePUTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(PUT, "application/vnd.default+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(PUT.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.default+json"}));
    }

    @Test
    public void shouldGenerateResourceInterfaceWithOnePATCHMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(PATCH, "application/vnd.default+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(PATCH.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.default+json"}));
    }

    @Test
    public void shouldGenerateResourceInterfaceWithOneSynchronousPOSTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(POST, "application/vnd.default+json")
                                        .withResponseTypes("application/vnd.ctx.query.query1+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")
                                                .withResponseType("application/vnd.ctx.query.query1+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        final List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(POST.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.default+json"}));
        assertThat(method.getAnnotation(Produces.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class).value(),
                is(new String[]{"application/vnd.ctx.query.query1+json"}));
    }

    @Test
    public void shouldGenerateResourceInterfaceWithOneSynchronousPUTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(PUT, "application/vnd.default+json")
                                        .withResponseTypes("application/vnd.ctx.query.query1+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")
                                                .withResponseType("application/vnd.ctx.query.query1+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        final List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(PUT.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.default+json"}));
        assertThat(method.getAnnotation(Produces.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class).value(),
                is(new String[]{"application/vnd.ctx.query.query1+json"}));
    }

    @Test
    public void shouldGenerateResourceInterfaceWithOneSynchronousPATCHMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(PATCH, "application/vnd.default+json")
                                        .withResponseTypes("application/vnd.ctx.query.query1+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")
                                                .withResponseType("application/vnd.ctx.query.query1+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        final List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(PATCH.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.default+json"}));
        assertThat(method.getAnnotation(Produces.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class).value(),
                is(new String[]{"application/vnd.ctx.query.query1+json"}));
    }

    @Test
    public void shouldGenerateResourceInterfaceWithOneGETMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(GET)
                                        .withResponseTypes("application/vnd.ctx.query.query1+json")
                                        .with(mapping().withResponseType("application/vnd.ctx.query.query1+json").withName("blah")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(javax.ws.rs.GET.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class).value(),
                is(new String[]{"application/vnd.ctx.query.query1+json"}));
    }


    @Test
    public void shouldGenerateGETMethodWithTwoMediaTypeAnnotations() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(httpAction(GET)
                                        .withResponseTypes(
                                                "application/vnd.ctx.query.query1+json",
                                                "application/vnd.ctx.query.query2+json")
                                        .with(mapping()
                                                .withName("blah1")
                                                .withResponseType("application/vnd.ctx.query.query1+json"))
                                        .with(mapping()
                                                .withName("blah2")
                                                .withResponseType("application/vnd.ctx.query.query2+json"))
                                )
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(javax.ws.rs.GET.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class).value(),
                arrayContainingInAnyOrder("application/vnd.ctx.query.query1+json", "application/vnd.ctx.query.query2+json"));

    }


    @Test
    public void shouldGenerateInterfaceThatContainsMethodWithBodyParameter() throws Exception {

        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));

    }

    @Test
    public void shouldGenerateInterfaceThatContainsMethodWithPathParamAndBodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path/{param1}")
                                .withPathParam("param1")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        assertThat(interfaceClass.isInterface(), is(true));
        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(2));

        Parameter methodParam1 = method.getParameters()[0];
        assertThat(methodParam1.getType(), equalTo(String.class));
        assertThat(methodParam1.getAnnotations(), arrayWithSize(1));
        assertThat(methodParam1.getAnnotations()[0].annotationType(), equalTo(PathParam.class));
        assertThat(methodParam1.getAnnotation(PathParam.class).value(), is("param1"));

        Parameter methodParam2 = method.getParameters()[1];
        assertThat(methodParam2.getType(), equalTo(JsonObject.class));
        assertThat(methodParam2.getAnnotations(), emptyArray());

    }

    @Test
    public void shouldGenerateInterfaceThatContainsMethodWithTwoPathParamsAndBodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path/{paramA}/abc/{paramB}")
                                .withPathParam("paramA")
                                .withPathParam("paramB")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(3));

        Parameter methodParam1 = method.getParameters()[0];
        assertThat(methodParam1.getType(), equalTo(String.class));
        assertThat(methodParam1.getAnnotations(), arrayWithSize(1));
        assertThat(methodParam1.getAnnotations()[0].annotationType(), equalTo(PathParam.class));
        assertThat(methodParam1.getAnnotation(PathParam.class).value(), is("paramA"));

        Parameter methodParam2 = method.getParameters()[1];
        assertThat(methodParam2.getType(), equalTo(String.class));
        assertThat(methodParam2.getAnnotations(), arrayWithSize(1));
        assertThat(methodParam2.getAnnotations()[0].annotationType(), equalTo(PathParam.class));
        assertThat(methodParam2.getAnnotation(PathParam.class).value(), is("paramB"));

        Parameter methodParam3 = method.getParameters()[2];
        assertThat(methodParam3.getType(), equalTo(JsonObject.class));
        assertThat(methodParam3.getAnnotations(), emptyArray());

    }

    @Test
    public void shouldGenerateResourceClassImplementingInterface() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceInterface = compiler.compiledInterfaceOf(BASE_PACKAGE);
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(resourceClass.isInterface(), is(false));
        assertThat(resourceClass.getGenericInterfaces(), arrayWithSize(1));
        assertThat(resourceClass.getGenericInterfaces()[0].getTypeName(), equalTo(resourceInterface.getTypeName()));

    }

    @Test
    public void shouldGenerateANonFinalPublicResourceClass() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(isFinal(resourceClass.getModifiers()), is(false));
        assertThat(isPublic(resourceClass.getModifiers()), is(true));
    }

    @Test
    public void shouldGenerateResourceClassContainingCommandAdapterAnnotation() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                        .with(resource("/some/path").withDefaultPostAction()
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(resourceClass.isInterface(), is(false));
        assertThat(resourceClass.getAnnotation(Adapter.class), not(nullValue()));
        assertThat(resourceClass.getAnnotation(Adapter.class).value(), is(Component.COMMAND_API));

    }

    @Test
    public void shouldGenerateResourceClassContainingQueryAdapterAnnotation() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .with(resource("/some/path")
                                .withDefaultPostAction()
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(resourceClass.isInterface(), is(false));
        assertThat(resourceClass.getAnnotation(Adapter.class), not(nullValue()));
        assertThat(resourceClass.getAnnotation(Adapter.class).value(), is(Component.QUERY_API));

    }

    @Test
    public void shouldGenerateResourceClassContainingOneMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(clazz.isInterface(), is(false));
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
    }

    @Test
    public void shouldGenerateResourceClassContainingFourMethods() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{p1}", "p1")
                                .with(httpAction(POST,
                                        "application/vnd.ctx.command.command-a+json",
                                        "application/vnd.ctx.command.command-b+json",
                                        "application/vnd.ctx.command.command-c+json",
                                        "application/vnd.ctx.command.command-d+json")
                                        .with(mapping().withName("blah1").withRequestType("application/vnd.ctx.command.command-a+json"))
                                        .with(mapping().withName("blah2").withRequestType("application/vnd.ctx.command.command-b+json"))
                                        .with(mapping().withName("blah3").withRequestType("application/vnd.ctx.command.command-c+json"))
                                        .with(mapping().withName("blah4").withRequestType("application/vnd.ctx.command.command-d+json"))

                                )
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathP1Resource");

        assertThat(clazz.isInterface(), is(false));
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(4));
    }

    @Test
    public void shouldGenerateClassContainingMethodWithPathParamAndBodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path/{paramA}")
                                .withPathParam("paramA")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAResource");

        assertThat(clazz.isInterface(), is(false));
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(2));

        Parameter methodParam1 = method.getParameters()[0];
        assertThat(methodParam1.getType(), equalTo(String.class));
        assertThat(methodParam1.getAnnotations(), emptyArray());

        Parameter methodParam2 = method.getParameters()[1];
        assertThat(methodParam2.getType(), equalTo(JsonObject.class));

    }

    @Test
    public void shouldGenerateClassContainingMethodWithThreePathParamsAndOneBodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path/{paramA}/{paramB}/{paramC}")
                                .withPathParam("paramA")
                                .withPathParam("paramB")
                                .withPathParam("paramC")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAParamBParamCResource");

        assertThat(clazz.isInterface(), is(false));
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(4));

        Parameter pathParam1 = method.getParameters()[0];
        assertThat(pathParam1.getType(), equalTo(String.class));
        assertThat(pathParam1.getAnnotations(), emptyArray());

        Parameter pathParam2 = method.getParameters()[1];
        assertThat(pathParam2.getType(), equalTo(String.class));
        assertThat(pathParam2.getAnnotations(), emptyArray());

        Parameter pathParam3 = method.getParameters()[2];
        assertThat(pathParam3.getType(), equalTo(String.class));
        assertThat(pathParam3.getAnnotations(), emptyArray());

        Parameter bodyParam = method.getParameters()[3];
        assertThat(bodyParam.getType(), equalTo(JsonObject.class));
        assertThat(bodyParam.getAnnotations(), emptyArray());

    }

    @Test
    public void shouldGenerateClassInSpecifiedPackage() throws Exception {
        String basePackageName = "uk.gov.test2";

        java.nio.file.Path outputPath = get(outputFolder.getRoot().getAbsolutePath());
        GeneratorConfig config = new GeneratorConfig(outputPath, outputPath, basePackageName, emptyMap(), Collections.singletonList(outputPath.getParent()));
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                config);

        Class<?> resourceImplementation = compiler.compiledClassOf(basePackageName, "resource", "DefaultSomePathResource");

        assertThat(resourceImplementation.getPackage().getName(), is(basePackageName + ".resource"));

    }

    @Test
    public void shouldGenerateResourceClassWithOnePOSTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> class1 = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        List<Method> methods = methodsOf(class1);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));
    }

    @Test
    public void shouldGenerateResourceClassWithOneSynchronousPOSTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(POST, "application/vnd.default+json")
                                        .withResponseTypes("application/vnd.ctx.query.query1+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")
                                                .withResponseType("application/vnd.ctx.query.query1+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> class1 = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        List<Method> methods = methodsOf(class1);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));
    }

    @Test
    public void shouldGenerateResourceClassWithOnePUTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPutResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> class1 = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        List<Method> methods = methodsOf(class1);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));
    }

    @Test
    public void shouldGenerateResourceClassWithOneSynchronousPUTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(PUT, "application/vnd.default+json")
                                        .withResponseTypes("application/vnd.ctx.query.query1+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")
                                                .withResponseType("application/vnd.ctx.query.query1+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> class1 = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        List<Method> methods = methodsOf(class1);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));
    }

    @Test
    public void shouldGenerateResourceClassWithOnePATCHMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPatchResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> class1 = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        List<Method> methods = methodsOf(class1);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));
    }

    @Test
    public void shouldGenerateResourceClassWithOneSynchronousPATCHMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction(PATCH, "application/vnd.default+json")
                                        .withResponseTypes("application/vnd.ctx.query.query1+json")
                                        .with(mapping()
                                                .withName("blah")
                                                .withRequestType("application/vnd.default+json")
                                                .withResponseType("application/vnd.ctx.query.query1+json")))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> class1 = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        List<Method> methods = methodsOf(class1);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));
    }

    @Test
    public void shouldAddInterceptorChainProcessorIfThereIsPOSTResourceInRAML() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field chainProcess = resourceClass.getDeclaredField(INTERCEPTOR_CHAIN_PROCESSOR);
        assertThat(chainProcess, not(nullValue()));
        assertThat(chainProcess.getType(), equalTo(InterceptorChainProcessor.class));
        assertThat(chainProcess.getAnnotation(Inject.class), not(nullValue()));
        assertThat(chainProcess.getModifiers(), is(0));
    }

    @Test
    public void shouldAddLoggerConstant() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field logger = resourceClass.getDeclaredField("LOGGER");
        assertThat(logger, not(nullValue()));
        assertThat(logger.getType(), equalTo(Logger.class));
        assertThat(isPrivate(logger.getModifiers()), is(true));
        assertThat(isStatic(logger.getModifiers()), is(true));
        assertThat(isFinal(logger.getModifiers()), is(true));
    }

    @Test
    public void shouldAddInterceptorChainProcessorIfThereIsGETResourceInRAML() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultGetResource()
                                .withRelativeUri("/some/path"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field dispatcher = resourceClass.getDeclaredField(INTERCEPTOR_CHAIN_PROCESSOR);
        assertThat(dispatcher, not(nullValue()));
        assertThat(dispatcher.getType(), equalTo(InterceptorChainProcessor.class));
        assertThat(dispatcher.getAnnotation(Inject.class), not(nullValue()));
        assertThat(dispatcher.getModifiers(), is(0));
    }

    @Test
    public void shouldAddActionMapperBean() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user").with(defaultGetAction())).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUserResource");

        Field mapping = resourceClass.getDeclaredField("actionMapper");
        assertThat(mapping, not(nullValue()));
        assertThat(mapping.getType(), equalTo(BasicActionMapper.class));
        assertThat(mapping.getAnnotation(Inject.class), not(nullValue()));
        assertThat(mapping.getAnnotation(Named.class), not(nullValue()));
        assertThat(mapping.getAnnotation(Named.class).value(), is("DefaultUserResourceActionMapper"));
        assertThat(mapping.getModifiers(), is(0));
    }


    @Test
    public void shouldAddHeadersContext() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field dispatcher = resourceClass.getDeclaredField("headers");
        assertThat(dispatcher, not(nullValue()));
        assertThat(dispatcher.getType(), equalTo(HttpHeaders.class));
        assertThat(dispatcher.getAnnotation(javax.ws.rs.core.Context.class), not(nullValue()));
        assertThat(dispatcher.getModifiers(), is(0));
    }

    @Test
    public void shouldAddAnnotatedRestProcessorProperty() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(defaultPostResource()
                                .withRelativeUri("/some/path")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field dispatcher = resourceClass.getDeclaredField("restProcessor");
        assertThat(dispatcher, not(nullValue()));
        assertThat(dispatcher.getType(), equalTo(RestProcessor.class));
        assertThat(dispatcher.getAnnotation(Inject.class), not(nullValue()));
        assertThat(dispatcher.getModifiers(), is(0));
    }

    @Test
    public void shouldGenerateClassContainingQueryParam() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults().with(
                        resource("/users").with(httpAction(GET)
                                .with(queryParam("surname"))
                                .withResponseTypes("application/vnd.people.query.search-users+json")
                                .with(mapping()
                                        .withName("blah")
                                        .withResponseType("application/vnd.people.query.search-users+json")))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap())
        );

        Class<?> implementation = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUsersResource");

        assertThat(implementation.isInterface(), is(false));


        Method method = firstMethodOf(implementation);

        Parameter param = method.getParameters()[0];
        assertThat(param.getType(), equalTo(String.class));
        assertThat(param.getAnnotations(), emptyArray());

        Class<?> iface = compiler.compiledInterfaceClassOf(BASE_PACKAGE, "resource", "UsersResource");

        Method interMethod = firstMethodOf(iface);

        Parameter interParam = interMethod.getParameters()[0];
        assertThat(interParam.getType(), equalTo(String.class));
        assertThat(interParam.getAnnotations().length, is(1));

        Annotation annotation = interParam.getAnnotations()[0];
        assertThat(annotation.annotationType(), equalTo(QueryParam.class));

    }

    @Test
    public void shouldGenerateClassContainingThreeQueryParams() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults().with(
                        resource("/users").with(httpAction(GET)
                                .with(queryParam("surname"), queryParam("firstname"), queryParam("middlename"))
                                .withResponseTypes("application/vnd.people.query.search-users+json")
                                .with(mapping()
                                        .withName("blah")
                                        .withResponseType("application/vnd.people.query.search-users+json"))
                        )
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap())
        );

        Class<?> implementation = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUsersResource");

        assertThat(implementation.isInterface(), is(false));

        Method method = firstMethodOf(implementation);
        assertThat(method.getParameterCount(), is(3));

        stream(method.getParameters()).forEach(parameter -> {
            assertThat(parameter.getType(), equalTo(String.class));
            assertThat(parameter.getAnnotations(), emptyArray());
        });

        Class<?> iface = compiler.compiledInterfaceClassOf(BASE_PACKAGE, "resource", "UsersResource");

        assertThat(iface.isInterface(), is(true));

        Method interMethod = firstMethodOf(iface);
        assertThat(interMethod.getParameterCount(), is(3));

        stream(interMethod.getParameters()).forEach(parameter -> {
            assertThat(parameter.getType(), equalTo(String.class));
            assertThat(parameter.getAnnotations().length, is(1));

            Annotation annotation = parameter.getAnnotations()[0];
            assertThat(annotation.annotationType(), equalTo(QueryParam.class));
        });
    }


    private Method methodWithConsumesAnnotationOf(List<Method> methods, String mediaType) {
        return methods.stream().filter(m -> m.getAnnotation(Consumes.class)
                .value()[0]
                .equals(mediaType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(format("No method consuming %s found", mediaType)));
    }


}
