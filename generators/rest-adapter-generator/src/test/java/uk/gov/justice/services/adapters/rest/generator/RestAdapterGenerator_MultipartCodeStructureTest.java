package uk.gov.justice.services.adapters.rest.generator;

import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MimeTypeBuilder.multipartWithFileFormParameter;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetailsFactory;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.Test;

public class RestAdapterGenerator_MultipartCodeStructureTest extends BaseRestAdapterGeneratorTest {

    private static final String RESOURCE_PACKAGE = BASE_PACKAGE + ".resource";

    @Test
    public void shouldGenerateResourceInterfaceWithOnePOSTMethod() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema(multipartWithFileFormParameter("photoId"))
                                        .with(mapping()
                                                .withName("upload")
                                                .withRequestType(MULTIPART_FORM_DATA)))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> interfaceClass = COMPILER.compiledInterfaceOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                RESOURCE_PACKAGE);

        final List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getReturnType(), equalTo(Response.class));
        assertThat(method.getAnnotation(javax.ws.rs.POST.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class), not(nullValue()));
        assertThat(method.getAnnotation(Consumes.class).value(),
                is(new String[]{MULTIPART_FORM_DATA}));
        assertThat(method.getAnnotation(Produces.class), is(nullValue()));
    }

    @Test
    public void shouldGenerateInterfaceThatContainsMethodWithMultipartParameter() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema(multipartWithFileFormParameter("photoId"))
                                        .with(mapping()
                                                .withName("upload")
                                                .withRequestType(MULTIPART_FORM_DATA)))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> interfaceClass = COMPILER.compiledInterfaceOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                RESOURCE_PACKAGE);

        final List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(1));
        final Parameter parameter = method.getParameters()[0];
        assertThat(parameter.getType(), equalTo(MultipartFormDataInput.class));
        assertThat(parameter.getAnnotation(MultipartForm.class), not(nullValue()));
    }

    @Test
    public void shouldGenerateInterfaceThatContainsMethodWithTwoPathParamsAndMultipartParameter() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/some/path")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema(multipartWithFileFormParameter("photoId"))
                                        .with(mapping()
                                                .withName("upload")
                                                .withRequestType(MULTIPART_FORM_DATA)))
                                .withRelativeUri("/some/path/{paramA}/abc/{paramB}")
                                .withPathParam("paramA")
                                .withPathParam("paramB")
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> interfaceClass = COMPILER.compiledInterfaceOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                RESOURCE_PACKAGE);

        final List<Method> methods = methodsOf(interfaceClass);
        assertThat(methods, hasSize(1));
        final Method method = methods.get(0);
        assertThat(method.getParameterCount(), is(3));

        final Parameter methodParam1 = method.getParameters()[0];
        assertThat(methodParam1.getType(), equalTo(String.class));
        assertThat(methodParam1.getAnnotations(), arrayWithSize(1));
        assertThat(methodParam1.getAnnotations()[0].annotationType(), equalTo(PathParam.class));
        assertThat(methodParam1.getAnnotation(PathParam.class).value(), is("paramA"));

        final Parameter methodParam2 = method.getParameters()[1];
        assertThat(methodParam2.getType(), equalTo(String.class));
        assertThat(methodParam2.getAnnotations(), arrayWithSize(1));
        assertThat(methodParam2.getAnnotations()[0].annotationType(), equalTo(PathParam.class));
        assertThat(methodParam2.getAnnotation(PathParam.class).value(), is("paramB"));

        final Parameter methodParam3 = method.getParameters()[2];
        assertThat(methodParam3.getType(), equalTo(MultipartFormDataInput.class));
        assertThat(methodParam3.getAnnotation(MultipartForm.class), not(nullValue()));
    }

    @Test
    public void shouldGenerateResourceClassImplementingMultipartInterface() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/some/path")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema(multipartWithFileFormParameter("photoId"))
                                        .with(mapping()
                                                .withName("upload")
                                                .withRequestType(MULTIPART_FORM_DATA)))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceInterface = COMPILER.compiledInterfaceOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                RESOURCE_PACKAGE);

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiSomePathResource");

        assertThat(resourceClass.isInterface(), is(false));
        assertThat(resourceClass.getGenericInterfaces(), arrayWithSize(1));
        assertThat(resourceClass.getGenericInterfaces()[0].getTypeName(), equalTo(resourceInterface.getTypeName()));
    }

    @Test
    public void shouldGenerateResourceClassWithInjectedFileInputDetailsFactory() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/some/path")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema(multipartWithFileFormParameter("photoId"))
                                        .with(mapping()
                                                .withName("upload")
                                                .withRequestType(MULTIPART_FORM_DATA)))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiSomePathResource");

        final Field chainProcess = resourceClass.getDeclaredField("fileInputDetailsFactory");
        assertThat(chainProcess, not(nullValue()));
        assertThat(chainProcess.getType(), equalTo(FileInputDetailsFactory.class));
        assertThat(chainProcess.getAnnotation(Inject.class), not(nullValue()));
        assertThat(chainProcess.getModifiers(), is(0));
    }
}
