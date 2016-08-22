package uk.gov.justice.services.generators.test.utils.builder;

import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultGetResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultPostResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.Raml;
import org.raml.model.Resource;

public class RamlBuilder {
    private final List<ResourceBuilder> resourceBuilders = new ArrayList<>();
    private String version;
    private String title;
    private String baseUri;

    public static RamlBuilder raml() {
        return new RamlBuilder();
    }

    public static RamlBuilder messagingRamlWithDefaults() {
        return new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withDefaultMessagingBaseUri();
    }

    public static RamlBuilder restRamlWithDefaults() {
        return restRamlWithTitleVersion()
                .withBaseUri("http://localhost:8080/warname/command/api/rest/service");
    }

    public static RamlBuilder restRamlWithCommandApiDefaults() {
        return restRamlWithTitleVersion()
                .withBaseUri("http://localhost:8080/warname/command/api/rest/service");
    }

    public static RamlBuilder restRamlWithEventApiDefaults() {
        return restRamlWithTitleVersion()
                .withBaseUri("http://localhost:8080/warname/event/api/rest/service");
    }

    public static RamlBuilder restRamlWithQueryApiDefaults() {
        return restRamlWithTitleVersion()
                .withBaseUri("http://localhost:8080/warname/query/api/rest/service");
    }

    public static RamlBuilder restRamlWithTitleVersion() {
        return new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service");
    }

    public RamlBuilder with(final ResourceBuilder resource) {
        resourceBuilders.add(resource);
        return this;
    }

    public RamlBuilder withDefaultPostResource() {
        return this.with(defaultPostResource());
    }

    public RamlBuilder withDefaultGetResource() {
        return this.with(defaultGetResource());
    }

    public RamlBuilder withDefaultMessagingResource() {
        return this
                .withDefaultMessagingBaseUri()
                .with(resource()
                        .withRelativeUri("/somecontext.controller.command")
                        .with(httpAction(POST, "application/vnd.somecontext.command1+json")));
    }

    public RamlBuilder withVersion(final String version) {
        this.version = version;
        return this;
    }

    public RamlBuilder withTitle(final String title) {
        this.title = title;
        return this;
    }

    public RamlBuilder withDefaultMessagingBaseUri() {
        return withBaseUri("message://event/processor/message/context");
    }

    public RamlBuilder withBaseUri(final String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public Raml build() {
        final Raml raml = new Raml();

        raml.setBaseUri(baseUri);
        raml.setVersion(version);
        raml.setTitle(title);

        final Map<String, Resource> resources = new HashMap<>();
        for (final ResourceBuilder resourceBuilder : resourceBuilders) {
            Resource resource = resourceBuilder.build();
            resources.put(resource.getRelativeUri(), resource);
        }

        raml.setResources(resources);
        return raml;
    }
}
