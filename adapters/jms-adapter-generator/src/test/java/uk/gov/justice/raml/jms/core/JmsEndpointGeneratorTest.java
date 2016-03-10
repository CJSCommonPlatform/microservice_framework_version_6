package uk.gov.justice.raml.jms.core;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapters.test.utils.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.ResourceBuilder.resource;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.raml.model.ActionType;
import org.raml.model.Raml;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapters.test.utils.JavaCompilerUtil;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.jms.AbstractJMSListener;

public class JmsEndpointGeneratorTest {

    private static final String BASE_PACKAGE = "uk.test";
    private static final String BASE_PACKAGE_FOLDER = "/uk/test";

    private Generator generator = new JmsEndpointGenerator();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    private JavaCompilerUtil compiler;

    @Before
    public void setup() throws Exception {
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldCreatePackageFolder() throws Exception {
        generator.run(new Raml(), configurationWithBasePackage(BASE_PACKAGE));

        String path = outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER;
        File packageDir = new File(path);
        assertThat(packageDir.exists(), is(true));
        assertThat(packageDir.isDirectory(), is(true));

    }

    @Test
    public void shouldCreateJmsClass() throws Exception {
        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller.commands"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        File[] files = packageDir.listFiles();
        assertThat(files.length, is(1));
        assertThat(files[0].getName(), is("StructureControllerCommandsJmsListener.java"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateMultipleJmsClasses() throws Exception {
        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller.commands"))
                        .with(resource()
                                .withRelativeUri("/people.controller.commands"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        File[] files = packageDir.listFiles();
        assertThat(files.length, is(2));
        assertThat(files,
                arrayContainingInAnyOrder(hasProperty("name", equalTo("PeopleControllerCommandsJmsListener.java")),
                        hasProperty("name", equalTo("StructureControllerCommandsJmsListener.java"))));

    }

    @Test
    public void shouldOverwriteJmsClass() throws Exception {
        String path = outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER;
        File packageDir = new File(path);
        packageDir.mkdirs();
        Files.write(Paths.get(path + "/StructureControllerCommandsJmsListener.java"),
                Arrays.asList("Old file content"));

        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller.commands"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE));

        List<String> lines = Files.readAllLines(Paths.get(path + "/StructureControllerCommandsJmsListener.java"));
        assertThat(lines.get(0), not(containsString("Old file content")));
    }

    @Test
    public void shouldCreateJmsEndpointNamedAfterResourceUri() throws Exception {
        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller.commands"))
                        .build(),
                configurationWithBasePackage("uk.somepackage"));

        Class<?> compiledClass = compiler.compiledClassOf("uk.somepackage");
        assertThat(compiledClass.getName(), is("uk.somepackage.StructureControllerCommandsJmsListener"));
    }

    @Test
    public void shouldCreateJmsEndpointInADifferentPackage() throws Exception {
        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/structure.controller.commands"))
                        .build(),
                configurationWithBasePackage("uk.package2"));

        Class<?> clazz = compiler.compiledClassOf("uk.package2");
        assertThat(clazz.getName(), is("uk.package2.StructureControllerCommandsJmsListener"));
    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithCommandHandlerApater() throws Exception {
        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/people.handler.commands")
                                .with(action().with(ActionType.POST)))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE));
        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE);
        Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(COMMAND_HANDLER));

    }

    @Test
    public void shouldCreateJmsEndpointAnnotatedWithCommandControllerApater() throws Exception {
        generator.run(
                raml()
                        .with(resource()
                                .withRelativeUri("/people.controller.commands")
                                .with(action().with(ActionType.POST)))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE);
        Adapter adapterAnnotation = clazz.getAnnotation(Adapter.class);
        assertThat(adapterAnnotation, not(nullValue()));
        assertThat(adapterAnnotation.value(), is(COMMAND_CONTROLLER));

    }

    @Test
    public void shouldCreateJmsEndpointExtendingAbstractJmsListener() throws Exception {
        generator.run(raml().withDefaults().build(), configurationWithBasePackage(BASE_PACKAGE));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE);
        assertThat(clazz.getSuperclass(), equalTo(AbstractJMSListener.class));
    }

    private GeneratorConfig configurationWithBasePackage(String basePackageName) {
        Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName);
    }

}
