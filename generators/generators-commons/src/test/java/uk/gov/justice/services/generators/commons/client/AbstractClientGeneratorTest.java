package uk.gov.justice.services.generators.commons.client;


import static java.lang.Boolean.FALSE;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.generators.commons.config.GeneratorPropertiesFactory.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.messagingRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AbstractClientGeneratorTest {

    private static final String EXISTING_FILE_PATH = "org/raml/test/resource/RemoteBCDController.java";
    private static final String LOGGER_FIELD = "logger";
    private static final String BASE_PACKAGE = "org.raml.test";
    private static final JavaCompilerUtility COMPILER = javaCompilerUtil();

    @Mock
    private Logger logger;

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    private final ABCClientGenerator generator = new ABCClientGenerator();

    @Before
    public void before() {
        overrideLogger(generator, logger);
    }

    @Test
    public void shouldGenerateRemotePostController() throws Exception {

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(POST, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_API")));


        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteABCController"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_API"));

    }

    @Test
    public void shouldGenerateRemotePutController() throws Exception {

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(PUT, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_API")));


        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteABCController"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_API"));

    }

    @Test
    public void shouldGenerateRemotePatchController() throws Exception {

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(PATCH, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_API")));


        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteABCController"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_API"));

    }

    @Test
    public void shouldGenerateRemoteDeleteController() throws Exception {

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(DELETE, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_API")));


        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteABCController"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_API"));

    }

    @Test
    public void shouldGenerateRemoteHandler() throws Exception {

        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(GET)
                                        .withResponseTypes("application/vnd.cakeshop.actionabc+json")))

                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withServiceComponentOf("COMMAND_CONTROLLER")));


        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        assertThat(generatedClass.getCanonicalName(), is("org.raml.test.RemoteABCController"));
        assertThat(generatedClass.getAnnotation(Remote.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class), not(nullValue()));
        assertThat(generatedClass.getAnnotation(FrameworkComponent.class).value(), is("COMMAND_CONTROLLER"));

    }

    @Test
    public void shouldContainLoggerConstant() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        final Field logger = generatedClass.getDeclaredField("LOGGER");
        assertThat(logger, not(nullValue()));
        assertThat(logger.getType(), equalTo(Logger.class));
        assertThat(isPrivate(logger.getModifiers()), is(true));
        assertThat(isStatic(logger.getModifiers()), is(true));
        assertThat(isFinal(logger.getModifiers()), is(true));
    }

    @Test
    public void shouldContainVariable() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        final Field logger = generatedClass.getDeclaredField("dummyVariable");
        assertThat(logger, not(nullValue()));
        assertThat(logger.getType(), equalTo(Object.class));
        assertThat(isStatic(logger.getModifiers()), is(false));
    }

    @Test
    public void shouldGenerateAnnotatedMethod() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        final List<Method> methods = methodsOf(generatedClass);
        assertThat(methods, hasSize(1));

        final Method method = methods.get(0);
        final Handles handlesAnnotation = method.getAnnotation(Handles.class);
        assertThat(handlesAnnotation, not(nullValue()));
        assertThat(handlesAnnotation.value(), is("some.action"));
        assertThat(method.getParameterCount(), is(1));
        assertThat(method.getParameters()[0].getType(), equalTo(JsonEnvelope.class));
    }

    @Test
    public void methodShouldReturnIntegerValue() throws Exception {
        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withDefaultServiceComponent()));

        final Class<?> generatedClass = COMPILER.compiledClassOf(
                outputFolder.getRoot(),
                outputFolder.getRoot(),
                BASE_PACKAGE,
                "RemoteABCController");

        final List<Method> methods = methodsOf(generatedClass);
        assertThat(methods, hasSize(1));

        final Object instance = generatedClass.newInstance();
        setField(instance, "traceLogger", new DefaultTraceLogger());

        final Method method = firstMethodOf(generatedClass).get();
        final Object result = method.invoke(instance, mock(JsonEnvelope.class));
        assertThat(result, is(12345678));
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotSet() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("serviceComponent generator property not set in the plugin config");

        generator.run(
                messagingRamlWithDefaults().withDefaultMessagingResource().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

    }

    @Test
    public void shouldThrowExceptionIfActionOtherThanPOSTorGET() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(containsString("Unsupported httpAction type TRACE"));
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(TRACE, "application/vnd.cakeshop.actionabc+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().withDefaultServiceComponent()));

    }


    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotGenerateExistingClasses() throws Exception {

        new BCDClientGenerator().run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(GET)))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withServiceComponentOf("COMMAND_CONTROLLER"), singletonList(existingFilePath())));

        final Path outputPath = Paths.get(outputFolder.newFile().getAbsolutePath(), EXISTING_FILE_PATH);

        assertThat(outputPath.toFile().exists(), equalTo(FALSE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldLogWarningIfClassExists() throws Exception {

        final Generator generator = new BCDClientGenerator();
        overrideLogger(generator, logger);
        generator.run(
                messagingRamlWithDefaults()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(GET)))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder,
                        generatorProperties().withServiceComponentOf("COMMAND_CONTROLLER"), singletonList(existingFilePath())));

        verify(logger).warn("The class {} already exists, skipping code generation.", "RemoteBCDController");

    }


    static class ABCClientGenerator extends AbstractClientGenerator {

        @Override
        protected String classNameOf(final Raml raml, final String serviceComponent) {
            return "RemoteABCController";
        }

        @Override
        protected Iterable<FieldSpec> fieldsOf(final Raml raml) {
            return ImmutableList.of(FieldSpec.builder(Object.class, "dummyVariable")
                    .build());
        }

        @Override
        protected TypeName methodReturnTypeOf(final Action ramlAction) {
            return TypeName.INT;
        }


        @Override
        protected CodeBlock methodBodyOf(final Resource resource, final Action ramlAction, final ActionMimeTypeDefinition definition) {
            return CodeBlock.builder().addStatement("return 12345678").build();
        }

        @Override
        protected String handlesAnnotationValueOf(final Action ramlAction, final ActionMimeTypeDefinition definition, final GeneratorConfig generatorConfig) {
            return "some.action";
        }
    }

    static class BCDClientGenerator extends ABCClientGenerator {
        @Override
        protected String classNameOf(final Raml raml, final String serviceComponent) {
            return "RemoteBCDController";
        }

    }

    private Path existingFilePath() {
        final URL resource = getClass().getClassLoader().getResource(EXISTING_FILE_PATH);
        return Paths.get(new File(resource.getPath()).getPath()).getParent().getParent().getParent().getParent().getParent();
    }

    private static void overrideLogger(final Generator<Raml> generator, final Logger logger) {
        try {
            final Field field = AbstractClientGenerator.class.getDeclaredField(LOGGER_FIELD);
            field.setAccessible(true);
            field.set(generator, logger);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not set logger on generator under test", e);
        }
    }
}
