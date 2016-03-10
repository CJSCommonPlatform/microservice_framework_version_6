package uk.gov.justice.raml.jms.core;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.raml.model.Raml;
import uk.gov.justice.raml.core.Configuration;
import uk.gov.justice.services.adapters.test.utils.JavaCompilerUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.raml.jms.core.util.RamlBuilder.raml;
import static uk.gov.justice.raml.jms.core.util.ResourceBuilder.resource;

public class JmsEndpointGeneratorTest {

    private Generator generator = new JmsEndpointGenerator();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    private JavaCompilerUtil compilerUtil;

    @Before
    public void setup() throws Exception {
        compilerUtil = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldCreatePackageFolder() throws Exception {
        final String basePackageName = "uk.test";
        Raml raml = new Raml();
        generator.run(raml, configurationWithBasePackage(basePackageName));

        String path = outputFolder.getRoot().getAbsolutePath() + "/uk/test";
        File packageDir = new File(path);
        assertThat(packageDir.exists(), is(true));
        assertThat(packageDir.isDirectory(), is(true));

    }

    @Test
    public void shouldCreateJmsClass() throws Exception {
        Raml raml = raml()
                .with(resource()
                        .withRelativeUri("/test/uri"))
                .build();

        generator.run(raml, configurationWithBasePackage("uk.test"));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + "/uk/test");
        File[] files = packageDir.listFiles();
        assertThat(files.length, is(1));
        assertThat(files[0].getName(), is("TestUriJmsListener.java"));
    }

    @Test
    public void shouldCreateMultipleJmsClasses() throws Exception {
        Raml raml = raml()
                .with(resource()
                        .withRelativeUri("/test/first"))
                .with(resource()
                        .withRelativeUri("/test/second"))
                .build();

        generator.run(raml, configurationWithBasePackage("uk.test"));

        File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + "/uk/test");
        File[] files = packageDir.listFiles();
        assertThat(files.length, is(2));
        assertThat(files[0].getName(), is("TestFirstJmsListener.java"));
        assertThat(files[1].getName(), is("TestSecondJmsListener.java"));
    }

    @Test
    public void shouldReturnListOfGeneratedFiles() throws Exception {
        Raml raml = raml()
                .with(resource()
                        .withRelativeUri("/test/first"))
                .with(resource()
                        .withRelativeUri("/test/second"))
                .build();

        Set<String> generatedFiles = generator.run(raml, configurationWithBasePackage("uk.test"));

        Iterator<String> iterator = generatedFiles.iterator();
        assertThat(iterator.next(), is("TestSecondJmsListener.java"));
        assertThat(iterator.next(), is("TestFirstJmsListener.java"));
    }

    @Test
    public void shouldOverwriteJmsClass() throws Exception {
        String path = outputFolder.getRoot().getAbsolutePath() + "/uk/test";
        File packageDir = new File(path);
        packageDir.mkdirs();
        Files.write(Paths.get(path + "/TestUriJmsListener.java"), Arrays.asList("Old file content"));

        Raml raml = raml()
                .with(resource()
                        .withRelativeUri("/test/uri"))
                .build();

        generator.run(raml, configurationWithBasePackage("uk.test"));

        List<String> lines = Files.readAllLines(Paths.get(path + "/TestUriJmsListener.java"));
        assertThat(lines.get(0), not(containsString("Old file content")));
    }

    @Test
    public void shouldCreateJmsEndpointNamedAfterResourceUri() throws Exception {
        Raml raml = raml()
                .with(resource()
                        .withRelativeUri("/test/uri"))
                .build();

        generator.run(raml, configurationWithBasePackage("uk.test"));

        Class<?> compiledClass = compilerUtil.compiledClassOf("uk.test");
        assertThat(compiledClass.getName(), is("uk.test.TestUriJmsListener"));
    }

    @Test
    public void shouldCreateJmsEndpointInADifferentPackage() throws Exception {
        Raml raml = raml()
                .with(resource()
                        .withRelativeUri("/test/uri"))
                .build();

        generator.run(raml, configurationWithBasePackage("uk.package2"));

        Class<?> compiledClass = compilerUtil.compiledClassOf("uk.package2");
        assertThat(compiledClass.getName(), is("uk.package2.TestUriJmsListener"));
    }

    private Configuration configurationWithBasePackage(String basePackageName) {
        Configuration configuration = new Configuration();
        configuration.setBasePackageName(basePackageName);
        configuration.setOutputDirectory(outputFolder.getRoot());
        return configuration;
    }

}
