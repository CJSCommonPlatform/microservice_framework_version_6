package uk.gov.justice.services.adapters.rest.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import org.apache.commons.lang.Validate;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.loader.UrlResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import uk.gov.justice.raml.core.Configuration;
import uk.gov.justice.raml.core.Generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.join;

public class DefaultGenerator implements Generator {

    private static String toDetailedString(ValidationResult item) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\t");
        stringBuilder.append(item.getLevel());
        stringBuilder.append(" ");
        stringBuilder.append(item.getMessage());
        if (item.getLine() != ValidationResult.UNKNOWN) {
            stringBuilder.append(" (line ");
            stringBuilder.append(item.getLine());
            if (item.getStartColumn() != ValidationResult.UNKNOWN) {
                stringBuilder.append(", col ");
                stringBuilder.append(item.getStartColumn());
                if (item.getEndColumn() != item.getStartColumn()) {
                    stringBuilder.append(" to ");
                    stringBuilder.append(item.getEndColumn());
                }
            }
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    @Override
    public Set<String> run(final String ramlBuffer, final Configuration configuration) {
        ResourceLoader[] loaderArray = prepareResourceLoaders(configuration);

        final List<ValidationResult> results = RamlValidationService
                .createDefault(new CompositeResourceLoader(loaderArray))
                .validate(ramlBuffer, "");

        if (ValidationResult.areValid(results)) {
            try {
                return run(new RamlDocumentBuilder(new CompositeResourceLoader(loaderArray)).build(ramlBuffer, ""),
                        configuration);
            } catch (JClassAlreadyExistsException | IOException e) {
                throw new IllegalArgumentException("Error processing RAML", e);
            }
        } else {
            final List<String> validationErrors = results.stream().map(DefaultGenerator::toDetailedString)
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("Invalid RAML definition:\n"
                    + join(validationErrors, "\n"));
        }
    }

    private Set<String> run(final Raml raml, final Configuration configuration)
            throws JClassAlreadyExistsException, IOException {
        validate(configuration);

        Collection<Resource> ramlResourceModels = raml.getResources().values();

        JaxRsResourceCodeGenerator resourceCode = new JaxRsResourceCodeGenerator(configuration);
        for (final Resource ramlResourceModel : ramlResourceModels) {
            JDefinedClass resourceInterface = resourceCode.createInterface(ramlResourceModel);
            resourceCode.createImplementation(resourceInterface);
        }
        return resourceCode.generate();
    }

    private ResourceLoader[] prepareResourceLoaders(
            final Configuration configuration) {
        File sourceDirectory = configuration.getSourceDirectory();
        ArrayList<ResourceLoader> loaderList = new ArrayList<ResourceLoader>(
                Arrays.asList(new UrlResourceLoader(),
                        new ClassPathResourceLoader()));
        if (sourceDirectory != null) {
            String sourceDirAbsPath = sourceDirectory.getAbsolutePath();
            loaderList.add(new FileResourceLoader(sourceDirAbsPath));
        }

        return loaderList
                .toArray(new ResourceLoader[loaderList.size()]);
    }

    private void validate(final Configuration configuration) {
        Validate.notNull(configuration, "configuration can't be null");

        final File outputDirectory = configuration.getOutputDirectory();
        Validate.notNull(outputDirectory, "outputDirectory can't be null");

        Validate.isTrue(outputDirectory.isDirectory(), outputDirectory
                + " is not a pre-existing directory");
        Validate.isTrue(outputDirectory.canWrite(), outputDirectory
                + " can't be written to");

        Validate.notEmpty(configuration.getBasePackageName(),
                "base package name can't be empty");
    }

}
