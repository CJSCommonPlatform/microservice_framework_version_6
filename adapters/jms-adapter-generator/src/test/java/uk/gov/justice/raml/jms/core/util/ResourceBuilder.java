package uk.gov.justice.raml.jms.core.util;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceBuilder {
    private final List<ActionBuilder> actionBuilders = new ArrayList<>();

    private String parentUri = "";
    private String relativeUri;

    public static ResourceBuilder resource() {
        return new ResourceBuilder();
    }

    public ResourceBuilder with(final ActionBuilder action) {
        actionBuilders.add(action);
        return this;
    }

    public ResourceBuilder withRelativeUri(final String uri) {
        relativeUri = uri;
        return this;
    }

    public Resource build() {
        final Resource resource = new Resource();
        resource.setParentUri(parentUri);
        resource.setRelativeUri(relativeUri);

        Map<ActionType, Action> actions = new HashMap<>();
        for (ActionBuilder actionBuilder : actionBuilders) {
            Action action = actionBuilder.build();
            actions.put(action.getType(), action);
        }

        resource.setActions(actions);
        return resource;
    }
}
