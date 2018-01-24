package uk.gov.justice.services.generators.test.utils.config;


import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.rules.TemporaryFolder;

public class GeneratorConfigUtil {

    public static GeneratorConfig configurationWithBasePackage(final String basePackageName,
                                                               final TemporaryFolder outputFolder,
                                                               final GeneratorProperties generatorProperties) {
        final Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName, generatorProperties, Collections.singletonList(outputPath.getParent()));
    }

    public static GeneratorConfig configurationWithBasePackage(final String basePackageName,
                                                               final TemporaryFolder outputFolder,
                                                               final GeneratorProperties generatorProperties,
                                                               final List<Path> sourcePaths) {
        final Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName, generatorProperties, sourcePaths);
    }

    public static GeneratorConfig emptyPathConfigurationWith(final GeneratorProperties generatorProperties) {
        return new GeneratorConfig(new File("").toPath(), new File("").toPath(), "", generatorProperties, Collections.emptyList());
    }
}
