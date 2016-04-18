package uk.gov.justice.services.adapters.test.utils.builder;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ParamType.STRING;

public class ActionBuilder {
    private ActionType actionType;
    private final Map<String, MimeType> body = new HashMap<>();
    private List<Response> responses = new LinkedList<>();
    private final Map<String, QueryParameter> queryParameters = new HashMap<>();

    public static ActionBuilder action() {
        return new ActionBuilder();
    }

    public static ActionBuilder defaultPostAction() {
        return action(POST, "application/vnd.structure.command.test-cmd+json");
    }

    public static ActionBuilder defaultGetAction() {
        return action().withActionType(GET).withDefaultResponseType();
    }

    public static ActionBuilder action(final ActionType actionType, final String... mimeTypes) {
        ActionBuilder actionBuilder = new ActionBuilder()
                .withActionType(actionType);
        for (String mimeType : mimeTypes) {
            actionBuilder = actionBuilder.withMediaType(mimeType);
        }
        return actionBuilder;
    }

    public ActionBuilder withActionType(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public ActionBuilder withActionOfDefaultRequestType() {
        return withMediaType("application/vnd.ctx.command.defcmd+json");
    }

    public ActionBuilder withDefaultResponseType() {
        return withActionWithResponseTypes("application/vnd.ctx.query.defquery+json");
    }

    public ActionBuilder withActionWithResponseTypes(final String... responseTypes) {
        Response response = new Response();
        Map<String, MimeType> respBody = new HashMap<>();
        for (String responseType : responseTypes) {
            respBody.put(responseType, new MimeType(responseType));
        }
        response.setBody(respBody);
        responses.add(response);
        return this;
    }

    public ActionBuilder withQueryParameters(final QueryParameter... queryParameters) {
        stream(queryParameters).forEach(queryParameter -> this.queryParameters.put(queryParameter.getDisplayName(), queryParameter));
        return this;
    }

    public ActionBuilder withQueryParameters(final String... paramNames) {
        stream(paramNames).forEach(paramName -> {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setDisplayName(paramName);
            queryParameter.setType(STRING);
            this.queryParameters.put(paramName, queryParameter);
        });

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

        HashMap<String, Response> responsesMap = new HashMap<>();
        this.responses.forEach(r -> responsesMap.put("200", r));
        action.setResponses(responsesMap);

        action.setQueryParameters(queryParameters);

        return action;
    }

}
