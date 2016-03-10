package uk.gov.justice.raml.jms.core.util;

import org.raml.model.Raml;
import org.raml.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RamlBuilder {
    private final List<ResourceBuilder> resourceBuilders = new ArrayList<>();

    public static RamlBuilder raml() {
        return new RamlBuilder();
    }

    public RamlBuilder with(final ResourceBuilder resource) {
        resourceBuilders.add(resource);
        return this;
    }

    public Raml build() {
        Raml raml = new Raml();

        Map<String, Resource> resources = new HashMap<>();
        for (ResourceBuilder resourceBuilder : resourceBuilders) {
            Resource resource = resourceBuilder.build();
            resources.put(resource.getRelativeUri(), resource);
        }

        raml.setResources(resources);
        return raml;
    }
}
