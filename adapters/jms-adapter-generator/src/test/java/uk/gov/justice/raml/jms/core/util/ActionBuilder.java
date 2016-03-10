package uk.gov.justice.raml.jms.core.util;

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

    public ActionBuilder with(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public ActionBuilder withBody(final MimeType mimeType) {
        body.put(mimeType.toString(), mimeType);
        return this;
    }

    public Action build() {
        final Action action = new Action();
        action.setType(actionType);
        action.setBody(body);
        return action;
    }
}
