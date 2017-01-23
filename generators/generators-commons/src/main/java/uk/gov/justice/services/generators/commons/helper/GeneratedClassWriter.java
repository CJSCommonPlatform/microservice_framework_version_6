package uk.gov.justice.services.generators.commons.helper;


import static java.lang.String.format;
import static uk.gov.justice.services.generators.commons.helper.Names.JAVA_FILENAME_SUFFIX;
import static uk.gov.justice.services.generators.commons.helper.Names.RESOURCE_PACKAGE_NAME;

import uk.gov.justice.raml.core.GeneratorConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;

public class GeneratedClassWriter {

    private GeneratedClassWriter() {}

    public static void writeClass(final GeneratorConfig configuration, final String packageName, final TypeSpec typeSpec, final Logger logger) {
        try {
            if (!classExists(configuration, typeSpec)) {
                JavaFile.builder(packageName, typeSpec)
                        .build()
                        .writeTo(configuration.getOutputDirectory());
            } else {
                logger.warn("The class {} already exists, skipping code generation.", typeSpec.name);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean classExists(final GeneratorConfig configuration, final TypeSpec typeSpec) {
        for (final Path path : configuration.getSourcePaths()) {
            final String pathname = format("%s/%s/%s", path.toString(),
                    basePackagePathFrom(configuration), resourcePathFrom(typeSpec));
            if (new File(pathname).exists()) {
                return true;
            }
        }
        return false;
    }

    private static String resourcePathFrom(final TypeSpec typeSpec) {
        return format("%s/%s%s", RESOURCE_PACKAGE_NAME, typeSpec.name, JAVA_FILENAME_SUFFIX);
    }

    private static String basePackagePathFrom(final GeneratorConfig configuration) {
        return configuration.getBasePackageName().replaceAll("\\.", "/");
    }
}
