package uk.gov.justice.services.adapters.rest.generator;

import com.google.common.reflect.TypeToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.nio.file.Paths.get;
import static java.util.Arrays.stream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.methodsOf;

public class RestAdapterGenerator_CodeStructureTest {

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    private JavaCompilerUtil compiler;

    private RestAdapterGenerator generator;

    @Before
    public void before() {
        generator = new RestAdapterGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldGenerateApplicationClass() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> applicationClass = compiler.compiledClassOf(BASE_PACKAGE, "CommandApiRestServiceApplication");

        assertThat(applicationClass.isInterface(), is(false));
    }

    @Test
    public void shouldGenerateExtendingApplicationClass() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> applicationClass = compiler.compiledClassOf(BASE_PACKAGE, "CommandApiRestServiceApplication");

        assertThat(Application.class.isAssignableFrom(applicationClass), is(true));
    }

    @Test
    public void shouldGenerateAnnotatedApplicationClass() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> applicationClass = compiler.compiledClassOf(BASE_PACKAGE, "CommandApiRestServiceApplication");

        assertThat(applicationClass.getAnnotation(ApplicationPath.class), not(nullValue()));
        assertThat(applicationClass.getAnnotation(ApplicationPath.class).value(), is("/command/api/rest/service"));
    }

    @Test
    public void shouldGenerateApplicationClassWithGetClassesMethod() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> applicationClass = compiler.compiledClassOf(BASE_PACKAGE, "CommandApiRestServiceApplication");

        Method method = applicationClass.getDeclaredMethod("getClasses");
        assertThat(method, not(nullValue()));
        assertThat(method.getParameterCount(), equalTo(0));
        assertThat(isPublic(method.getModifiers()), is(true));
        assertThat(isStatic(method.getModifiers()), is(false));
        assertThat(isAbstract(method.getModifiers()), is(false));
        assertThat(method.getGenericReturnType(), equalTo(new TypeToken<Set<Class<?>>>() {
        }.getType()));
    }

    @Test
    public void shouldGenerateAnnotatedResourceInterface() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        assertThat(interfaceClass.isInterface(), is(true));
        assertThat(interfaceClass.getAnnotation(Path.class), not(nullValue()));
        assertThat(interfaceClass.getAnnotation(Path.class).value(), is("some/path"));
    }

    @Test
    public void shouldGenerateInterfaceInSpecifiedPackage() throws Exception {
        String basePackageName = "uk.gov.test1";

        java.nio.file.Path outputPath = get(outputFolder.getRoot().getAbsolutePath());
        GeneratorConfig config = new GeneratorConfig(outputPath, outputPath, basePackageName);
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                config);

        Class<?> interfaceClass = compiler.compiledInterfaceOf(basePackageName);

        assertThat(interfaceClass.getPackage().getName(), is(basePackageName + ".resource"));

    }

    @Test
    public void shouldGenerateResourceInterfaceWithOnePOSTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(POST.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.ctx.commands.default+json"}));
    }


    @Test
    public void shouldGenerateResourceInterfaceWithTwoPOSTMethods() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{p1}")
                                .with(action(POST, "application/vnd.ctx.commands.cmd-a+json", "application/vnd.ctx.commands.cmd-b+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(2));
        Method method1 = methodWithConsumesAnnotationOf(methods, "application/vnd.ctx.commands.cmd-a+json");

        assertThat(method1.getReturnType(), equalTo(Response.class));
        assertThat(method1.getAnnotation(POST.class), not(nullValue()));
        assertThat(method1.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method1.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.ctx.commands.cmd-a+json"}));

        Method method2 = methodWithConsumesAnnotationOf(methods, "application/vnd.ctx.commands.cmd-b+json");
        assertThat(method2.getReturnType(), equalTo(Response.class));
        assertThat(method2.getAnnotation(POST.class), not(nullValue()));
        assertThat(method2.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method2.getAnnotation(Consumes.class).value(),
                is(new String[]{"application/vnd.ctx.commands.cmd-b+json"}));

    }

    @Test
    public void shouldGenerateResourceInterfaceWithOneGETMethod() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(GET).withResponse("application/vnd.ctx.queries.query1+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(javax.ws.rs.GET.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class).value(),
                is(new String[]{"application/vnd.ctx.queries.query1+json"}));
    }


    @Test
    public void shouldGenerateGETMethodWithTwoMediaTypeAnnotations() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(GET)
                                        .withResponse(
                                                "application/vnd.ctx.queries.query1+json",
                                                "application/vnd.ctx.queries.query2+json")
                                )
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(javax.ws.rs.GET.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class), not(nullValue()));
        assertThat(method.getAnnotation(Produces.class).value(),
                arrayContainingInAnyOrder("application/vnd.ctx.queries.query1+json", "application/vnd.ctx.queries.query2+json"));

    }


    @Test
    public void interfaceShouldContainMethodWithBodyParameter() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/no/path/params")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> interfaceClass = compiler.compiledInterfaceOf(BASE_PACKAGE);

        List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));

    }

    @Test
    public void interfaceShouldContainMethodWithPathParamAndBodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

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
        assertThat(methodParam1.getAnnotation(PathParam.class).value(), is("paramA"));

        Parameter methodParam2 = method.getParameters()[1];
        assertThat(methodParam2.getType(), equalTo(JsonObject.class));
        assertThat(methodParam2.getAnnotations(), emptyArray());

    }

    @Test
    public void interfaceShouldContainMethodWithTwoPathParamsAndBodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}/abc/{paramB}", "paramA", "paramB")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

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
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceInterface = compiler.compiledInterfaceOf(BASE_PACKAGE);
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(resourceClass.isInterface(), is(false));
        assertThat(resourceClass.getGenericInterfaces(), arrayWithSize(1));
        assertThat(resourceClass.getGenericInterfaces()[0].getTypeName(), equalTo(resourceInterface.getTypeName()));

    }

    @Test
    public void shouldGenerateResourceClassContainingCommandApiAdapterAnnotation() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(resourceClass.isInterface(), is(false));
        assertThat(resourceClass.getAnnotation(Adapter.class), not(nullValue()));
        assertThat(resourceClass.getAnnotation(Adapter.class).value(), is(Component.COMMAND_API));

    }

    @Test
    public void shouldGenerateResourceClassContainingQueryApiAdapterAnnotation() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults().with(
                        resource("/some/path")
                                .with(action(GET)
                                        .withResponse("application/vnd.ctx.queries.query1+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(resourceClass.isInterface(), is(false));
        assertThat(resourceClass.getAnnotation(Adapter.class), not(nullValue()));
        assertThat(resourceClass.getAnnotation(Adapter.class).value(), is(Component.QUERY_API));

    }

    @Test
    public void shouldGenerateResourceClassContainingOneMethod() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertThat(clazz.isInterface(), is(false));
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
    }

    @Test
    public void shouldGenerateResourceClassContaining4Methods() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{p1}", "p1")
                                .with(action(POST,
                                        "application/vnd.ctx.commands.command-a+json",
                                        "application/vnd.ctx.commands.command-b+json",
                                        "application/vnd.ctx.commands.command-c+json",
                                        "application/vnd.ctx.commands.command-d+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathP1Resource");

        assertThat(clazz.isInterface(), is(false));
        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(4));
    }

    @Test
    public void classShouldContainMethodWithPathParamAndBodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(action(POST, "application/vnd.ctx.commands.command-a+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

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
    public void classShouldContainMethodWith3PathParamsAnd1BodyParam() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}/{paramB}/{paramC}", "paramA", "paramB", "paramC")
                                .with(action(POST, "application/vnd.ctx.commands.command-a+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

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
    public void classShouldContainQueryParam() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults().with(
                        resource("/users").with(action(GET)
                                .withQueryParameters("surname")
                                .withResponse("application/vnd.people.queries.search-users+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder)
        );

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUsersResource");

        assertThat(clazz.isInterface(), is(false));

        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(1));

        Parameter param = method.getParameters()[0];
        assertThat(param.getType(), equalTo(String.class));
        assertThat(param.getAnnotations(), emptyArray());

        Class<?> inter = compiler.compiledInterfaceClassOf(BASE_PACKAGE, "resource", "UsersResource");

        assertThat(inter.isInterface(), is(true));
        List<Method> interMethods = methodsOf(inter);
        assertThat(methods, hasSize(1));
        Method interMethod = interMethods.get(0);
        assertThat(method.getParameterCount(), is(1));

        Parameter interParam = interMethod.getParameters()[0];
        assertThat(interParam.getType(), equalTo(String.class));
        assertThat(interParam.getAnnotations().length, is(1));

        Annotation annotation = interParam.getAnnotations()[0];
        assertThat(annotation.annotationType(), equalTo(QueryParam.class));

    }

    @Test
    public void classShouldContainThreeQueryParams() throws Exception {
        generator.run(
                restRamlWithQueryApiDefaults().with(
                        resource("/users").with(action(GET)
                                .withQueryParameters("surname")
                                .withQueryParameters("firstname")
                                .withQueryParameters("middlename")
                                .withResponse("application/vnd.people.queries.search-users+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder)
        );

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultUsersResource");

        assertThat(clazz.isInterface(), is(false));

        List<Method> methods = methodsOf(clazz);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(3));

        stream(method.getParameters()).forEach(parameter -> {
            assertThat(parameter.getType(), equalTo(String.class));
            assertThat(parameter.getAnnotations(), emptyArray());
        });

        Class<?> inter = compiler.compiledInterfaceClassOf(BASE_PACKAGE, "resource", "UsersResource");

        assertThat(inter.isInterface(), is(true));
        List<Method> interMethods = methodsOf(inter);
        assertThat(methods, hasSize(1));
        Method interMethod = interMethods.get(0);
        assertThat(method.getParameterCount(), is(3));

        stream(interMethod.getParameters()).forEach(parameter -> {
            assertThat(parameter.getType(), equalTo(String.class));
            assertThat(parameter.getAnnotations().length, is(1));

            Annotation annotation = parameter.getAnnotations()[0];
            assertThat(annotation.annotationType(), equalTo(QueryParam.class));
        });
    }

    @Test
    public void shouldGenerateClassInSpecifiedPackage() throws Exception {
        String basePackageName = "uk.gov.test2";

        java.nio.file.Path outputPath = get(outputFolder.getRoot().getAbsolutePath());
        GeneratorConfig config = new GeneratorConfig(outputPath, outputPath, basePackageName);
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                config);

        Class<?> resourceImplementation = compiler.compiledClassOf(basePackageName, "resource", "DefaultSomePathResource");

        assertThat(resourceImplementation.getPackage().getName(), is(basePackageName + ".resource"));

    }

    @Test
    public void shouldGenerateEJBAnnotatedClass() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
        Class<?> resourceImplementation = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        assertThat(resourceImplementation.getAnnotation(Stateless.class), not(nullValue()));
    }

    @Test
    public void shouldGenerateResourceClassWithOnePOSTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> class1 = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");
        List<Method> methods = methodsOf(class1);
        assertThat(methods, hasSize(1));
        Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonObject.class));
    }

    @Test
    public void shouldAddAsyncDispatcherBeanIfThereIsPOSTResourceInRAML() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field dispatcher = resourceClass.getDeclaredField("asyncDispatcher");
        assertThat(dispatcher, not(nullValue()));
        assertThat(dispatcher.getType(), equalTo(AsynchronousDispatcher.class));
        assertThat(dispatcher.getAnnotation(Inject.class), not(nullValue()));
        assertThat(dispatcher.getModifiers(), is(0));
    }

    @Test
    public void shouldNotAddAsyncDispatcherBeanIfThereIsNoPOSTResourceInRAML() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(GET)
                                        .withResponse("application/vnd.ctx.queries.query1+json")
                                ))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertFalse(stream(resourceClass.getDeclaredFields())
                .filter(f -> f.getName().equals("asyncDispatcher"))
                .findAny()
                .isPresent());
    }


    @Test
    public void shouldAddSyncDispatcherBeanIfThereIsGETResourceInRAML() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(
                                resource("/some/path")
                                        .with(action(GET)
                                                .withResponse("application/vnd.ctx.queries.query1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field dispatcher = resourceClass.getDeclaredField("syncDispatcher");
        assertThat(dispatcher, not(nullValue()));
        assertThat(dispatcher.getType(), equalTo(SynchronousDispatcher.class));
        assertThat(dispatcher.getAnnotation(Inject.class), not(nullValue()));
        assertThat(dispatcher.getModifiers(), is(0));
    }

    @Test
    public void shouldNotAddSyncDispatcherBeanIfThereIsNoGETResourceInRAML() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        assertFalse(stream(resourceClass.getDeclaredFields())
                .filter(f -> f.getName().equals("syncDispatcher"))
                .findAny()
                .isPresent());

    }


    @Test
    public void shouldAddHeadersContext() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
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
                restRamlWithDefaults().with(
                        resource("/some/path")
                                .with(action(POST, "application/vnd.ctx.commands.default+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathResource");

        Field dispatcher = resourceClass.getDeclaredField("restProcessor");
        assertThat(dispatcher, not(nullValue()));
        assertThat(dispatcher.getType(), equalTo(RestProcessor.class));
        assertThat(dispatcher.getAnnotation(Inject.class), not(nullValue()));
        assertThat(dispatcher.getModifiers(), is(0));
    }


    private Method methodWithConsumesAnnotationOf(List<Method> methods, String mediaType) {
        return methods.stream().filter(m -> m.getAnnotation(Consumes.class)
                .value()[0]
                .equals(mediaType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(format("No method consuming %s found", mediaType)));
    }


}
