package uk.gov.justice.services.adapters.rest.util.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static java.text.MessageFormat.format;
import static uk.gov.justice.services.adapters.rest.util.builder.RamlResourceBuilder.aResource;

public class RamlBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RamlBuilder.class);
    private static String RAML_TEMPLATE = "#%RAML 0.8\r\n" +
            "title: Example Service\r\n" +
            "\r\n" +
            "{0} ";

    private List<RamlResourceBuilder> resources = new LinkedList<>();

    public static RamlBuilder aRaml() {
        return new RamlBuilder();
    }

    public RamlBuilder withDefaults() {
        return this.with(aResource());
    }

    public RamlBuilder with(RamlResourceBuilder resource) {
        resources.add(resource);
        return this;
    }

    public String toString() {
        StringBuilder resourcesRamlString = new StringBuilder();
        resources.forEach(r -> resourcesRamlString.append(r.toString()));
        String raml = format(RAML_TEMPLATE, resourcesRamlString);
        LOG.debug(System.lineSeparator() + raml);
        return raml;

    }

}
