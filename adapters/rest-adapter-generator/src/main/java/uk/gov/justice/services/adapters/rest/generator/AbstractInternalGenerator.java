package uk.gov.justice.services.adapters.rest.generator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.raml.model.Resource;

abstract class AbstractInternalGenerator {
    /**
     * Generate Java code for a Raml structure
     *
     * @param raml {@link Raml ) structure to generate code from
     * @return a list of {@link TypeSpec } that represent Java classes
     */
    public List<TypeSpec> generateFor(final Raml raml) {
        final Collection<Resource> resources = raml.getResources().values();
        return resources.stream()
                .map(this::generateFor)
                .collect(toList());
    }


    /**
     * Generate Java code for the given resource
     *
     * @param resource to generate as implementation classes
     * @return a list of {@link TypeSpec} that represent Java classes
     */
    abstract TypeSpec generateFor(final Resource resource);
}
