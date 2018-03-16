package uk.gov.justice.services.generators.commons.config;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;

import org.apache.maven.plugins.annotations.Parameter;

public class CommonGeneratorProperties implements GeneratorProperties {

    @Parameter
    private String serviceComponent;

    @Parameter
    private String customMDBPool;

    public String getCustomMDBPool() {
        return customMDBPool;
    }

    public String getServiceComponent() {
        return serviceComponent;
    }
}
