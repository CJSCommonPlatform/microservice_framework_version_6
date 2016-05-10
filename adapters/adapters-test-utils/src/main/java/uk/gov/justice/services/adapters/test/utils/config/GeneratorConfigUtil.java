package uk.gov.justice.services.adapters.test.utils.config;

import uk.gov.justice.raml.core.GeneratorConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.rules.TemporaryFolder;

public class GeneratorConfigUtil {

    public static GeneratorConfig configurationWithBasePackage(final String basePackageName,
                                                               final TemporaryFolder outputFolder,
                                                               final Map<String, String> generatorProperties) {
        Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName, generatorProperties, Collections.singletonList(outputPath.getParent()));
    }

    public static GeneratorConfig configurationWithBasePackage(final String basePackageName,
                                                               final TemporaryFolder outputFolder,
                                                               final Map<String, String> generatorProperties,
                                                               final List<Path> sourcePaths) {
        Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName, generatorProperties, sourcePaths);
    }

    public static GeneratorConfig emptyPathConfigurationWith(final Map<String, String> generatorProperties) {
        return new GeneratorConfig(new File("").toPath(), new File("").toPath(), "", generatorProperties, Collections.emptyList());
    }
}
