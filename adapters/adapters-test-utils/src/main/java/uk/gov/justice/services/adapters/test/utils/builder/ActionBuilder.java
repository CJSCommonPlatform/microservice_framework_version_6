package uk.gov.justice.services.adapters.test.utils.builder;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

import java.util.HashMap;
import java.util.Map;

public class ActionBuilder {
    private ActionType actionType;
    private final Map<String, MimeType> body = new HashMap<>();

    public static ActionBuilder action() {
        return new ActionBuilder();
    }

    public static ActionBuilder action(final ActionType actionType, final String... mimeTypes) {
        ActionBuilder actionBuilder = new ActionBuilder()
                .with(actionType);
        for (String mimeType: mimeTypes) {
            actionBuilder = actionBuilder.withMediaType(mimeType);
        }
        return actionBuilder;
    }

    public ActionBuilder with(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public ActionBuilder withMediaType(final MimeType mimeType) {
        body.put(mimeType.toString(), mimeType);
        return this;
    }
    public ActionBuilder withMediaType(String stringMimeType) {
        return withMediaType(new MimeType(stringMimeType));
    }

    public Action build() {
        final Action action = new Action();
        action.setType(actionType);
        action.setBody(body);
        return action;
    }

}
