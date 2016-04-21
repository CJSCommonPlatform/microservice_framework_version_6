package uk.gov.justice.services.adapters.test.utils.builder;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.defaultGetAction;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.defaultPostAction;

public class ResourceBuilder {
    private final List<ActionBuilder> actionBuilders = new ArrayList<>();

    private String parentUri = "";
    private String relativeUri = "/somecontext.controller.command";
    private Map<String, UriParameter> uriParameters = new HashMap<>();

    public static ResourceBuilder resource() {
        return new ResourceBuilder();
    }

    public static ResourceBuilder resource(final String relativeUri, final String... pathParams) {
        ResourceBuilder resourceBuilder = new ResourceBuilder()
                .withRelativeUri(relativeUri);
        for (String pathParam : pathParams) {
            resourceBuilder = resourceBuilder.withPathParam(pathParam);
        }
        return resourceBuilder;
    }

    public static ResourceBuilder defaultPostResource() {
        return resource("/some/path/{recipeId}")
                .with(defaultPostAction());

    }

    public static ResourceBuilder defaultGetResource() {
        return resource("/some/path/{recipeId}")
                .with(defaultGetAction());

    }

    public ResourceBuilder with(final ActionBuilder action) {
        actionBuilders.add(action);
        return this;
    }

    public ResourceBuilder withDefaultAction() {
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

        Map<ActionType, Action> actions = new HashMap<>();
        for (ActionBuilder actionBuilder : actionBuilders) {
            Action action = actionBuilder.build();
            action.setResource(resource);
            actions.put(action.getType(), action);
        }

        resource.setActions(actions);
        return resource;
    }

}
