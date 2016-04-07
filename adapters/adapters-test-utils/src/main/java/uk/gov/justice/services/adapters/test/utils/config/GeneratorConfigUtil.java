package uk.gov.justice.services.adapters.test.utils.config;

import org.junit.rules.TemporaryFolder;
import uk.gov.justice.raml.core.GeneratorConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneratorConfigUtil {
    
    public static GeneratorConfig configurationWithBasePackage(String basePackageName, TemporaryFolder outputFolder) {
        Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName);
    }

}
