package uk.gov.justice.raml.maven.lintchecker.rules.configuration;

import uk.gov.justice.raml.io.files.parser.RamlFileParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.raml.model.Raml;

public class TestConfiguration {

    private TestConfiguration() {}

    public Raml ramlGET() {
        return getRaml("src/test/resources/raml/all-match", "test.raml");
    }

    public Raml ramlGETmissing() {
        return getRaml("src/test/resources/raml/missing-handlers", "missing.handlers.raml");
    }

    private Raml getRaml(final String folder, final String file) {
        final RamlFileParser ramlFileParser = new RamlFileParser();

        final Path basePkg = Paths.get(folder);
        final Collection<Path> ramlPaths = new ArrayList<Path>();

        ramlPaths.add(Paths.get(file));

        return ramlFileParser.ramlOf(basePkg, ramlPaths)
                .iterator()
                .next();
    }

    public String basePackage() {
        return "uk.gov.justice.services.raml.lintcheck.handlers";
    }

    public static TestConfiguration testConfig() {
        return new TestConfiguration();
    }
}