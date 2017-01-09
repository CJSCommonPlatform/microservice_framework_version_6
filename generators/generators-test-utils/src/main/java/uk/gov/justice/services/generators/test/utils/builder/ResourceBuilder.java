package uk.gov.justice.services.generators.test.utils.builder;

import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultPostAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultPutAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;

public class ResourceBuilder {
    private final List<HttpActionBuilder> httpActionBuilders = new ArrayList<>();

    private String parentUri = "";
    private String relativeUri = "/somecontext.controller.command";
    private Map<String, UriParameter> uriParameters = new HashMap<>();

    public static ResourceBuilder resource() {
        return new ResourceBuilder();
    }

    public static ResourceBuilder resource(final String relativeUri, final String... pathParams) {
        ResourceBuilder resourceBuilder = new ResourceBuilder()
                .withRelativeUri(relativeUri);
        for (final String pathParam : pathParams) {
            resourceBuilder = resourceBuilder.withPathParam(pathParam);
        }
        return resourceBuilder;
    }

    public static ResourceBuilder defaultPostResource() {
        return resource("/some/path/{recipeId}")
                .with(defaultPostAction());

    }

    public static ResourceBuilder defaultPutResource() {
        return resource("/some/path/{recipeId}")
                .with(defaultPutAction());

    }

    public static ResourceBuilder defaultGetResource() {
        return resource("/some/path/{recipeId}")
                .with(defaultGetAction());

    }

    public ResourceBuilder with(final HttpActionBuilder action) {
        httpActionBuilders.add(action);
        return this;
    }

    public ResourceBuilder withDefaultPostAction() {
        with(defaultPostAction());
        return this;
    }

    public ResourceBuilder withRelativeUri(final String uri) {
        relativeUri = uri;
        return this;
    }

    public ResourceBuilder withPathParam(final String name) {
        uriParameters.put(name, new UriParameter(name));
        return this;
    }

    public Resource build() {
        final Resource resource = new Resource();
        resource.setParentUri(parentUri);
        resource.setRelativeUri(relativeUri);
        resource.setUriParameters(uriParameters);

        final Map<ActionType, Action> actions = new HashMap<>();
        for (final HttpActionBuilder httpActionBuilder : httpActionBuilders) {
            final Action action = httpActionBuilder.build();
            action.setResource(resource);
            actions.put(action.getType(), action);
        }

        resource.setActions(actions);
        return resource;
    }

}
