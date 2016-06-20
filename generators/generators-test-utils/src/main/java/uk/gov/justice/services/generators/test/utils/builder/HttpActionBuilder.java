package uk.gov.justice.services.generators.test.utils.builder;

import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static javax.ws.rs.core.Response.Status.OK;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.defaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;

/**
 * Builds RAML http action (not to be confused with framework's action)
 */
public class HttpActionBuilder {

    private final Map<String, MimeType> body = new HashMap<>();
    private final Map<String, QueryParameter> queryParameters = new HashMap<>();
    private final List<Response> responses = new ArrayList<>();
    private ActionType actionType;
    private String description;
    private MappingDescriptionBuilder mappingDescription;

    public static HttpActionBuilder httpAction() {
        return new HttpActionBuilder();
    }

    public static HttpActionBuilder defaultPostAction() {
        return httpAction()
                .withHttpActionType(POST)
                .withHttpActionOfDefaultRequestType();
    }

    public static HttpActionBuilder defaultGetAction() {
        return httpAction()
                .withHttpActionType(GET)
                .withDefaultResponseType();
    }

    public static HttpActionBuilder httpAction(final ActionType actionType, final String... mimeTypes) {
        HttpActionBuilder httpActionBuilder = new HttpActionBuilder()
                .withHttpActionType(actionType)
                .with(defaultMapping());
        for (final String mimeType : mimeTypes) {
            httpActionBuilder = httpActionBuilder.withMediaType(mimeType);
        }
        return httpActionBuilder;
    }

    public static HttpActionBuilder httpActionWithNoMapping(final ActionType actionType, final String... mimeTypes) {
        HttpActionBuilder httpActionBuilder = new HttpActionBuilder()
                .withHttpActionType(actionType);
        for (final String mimeType : mimeTypes) {
            httpActionBuilder = httpActionBuilder.withMediaType(mimeType);
        }
        return httpActionBuilder;
    }

    public HttpActionBuilder withHttpActionType(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public HttpActionBuilder withHttpActionOfDefaultRequestType() {
        return withMediaType("application/vnd.ctx.command.defcmd+json")
                .with(mapping()
                        .withName("action1")
                        .withRequestType("application/vnd.ctx.command.defcmd+json"));
    }

    public HttpActionBuilder withDefaultResponseType() {
        return withResponseTypes("application/vnd.ctx.query.defquery+json")
                .with(mapping()
                        .withName("action1")
                        .withResponseType("application/vnd.ctx.query.defquery+json"));
    }

    public HttpActionBuilder withResponseTypes(final String... responseTypes) {
        return withHttpActionResponse(new Response(), responseTypes);
    }

    public HttpActionBuilder withHttpActionResponse(final Response response, final String... responseTypes) {
        final Map<String, MimeType> respBody = new HashMap<>();
        for (final String responseType : responseTypes) {
            respBody.put(responseType, new MimeType(responseType));
        }
        response.setBody(respBody);
        responses.add(response);
        return this;
    }

    public HttpActionBuilder withQueryParameters(final QueryParameter... queryParameters) {
        stream(queryParameters).forEach(queryParameter -> this.queryParameters.put(queryParameter.getDisplayName(), queryParameter));
        return this;
    }

    public HttpActionBuilder with(final QueryParamBuilder... params) {
        for (QueryParamBuilder param : params) {
            final QueryParameter p = param.build();
            this.queryParameters.put(p.getDisplayName(), p);
        }
        return this;
    }

    public HttpActionBuilder withMediaType(final MimeType mimeType) {
        body.put(mimeType.toString(), mimeType);
        return this;
    }

    public HttpActionBuilder withMediaType(final String stringMimeType) {
        return withMediaType(new MimeType(stringMimeType));
    }

    public HttpActionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public HttpActionBuilder with(final MappingBuilder... mapping) {
        if (mappingDescription == null) {
            mappingDescription = mappingDescription();
        }

        this.mappingDescription.with(mapping);
        return this;
    }

    public Action build() {
        final Action action = new Action();
        action.setType(actionType);

        if (description != null) {
            action.setDescription(description);
        } else if (mappingDescription != null) {
            action.setDescription(mappingDescription.build());
        }

        action.setBody(body);

        final HashMap<String, Response> responsesMap = new HashMap<>();
        this.responses.forEach(r -> responsesMap.put(valueOf(OK.getStatusCode()), r));
        action.setResponses(responsesMap);
        action.setQueryParameters(queryParameters);
        return action;
    }

}
