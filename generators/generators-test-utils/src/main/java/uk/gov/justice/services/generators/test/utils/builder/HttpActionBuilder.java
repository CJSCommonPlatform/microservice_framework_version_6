package uk.gov.justice.services.generators.test.utils.builder;

import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static javax.ws.rs.core.Response.Status.OK;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.defaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescription;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.builder.ResponseBuilder.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;

/**
 * Builds RAML http action (not to be confused with framework's action)
 */
public class HttpActionBuilder {

    private final Map<String, MimeType> body = new HashMap<>();
    private final Map<String, QueryParameter> queryParameters = new HashMap<>();
    private final List<Response> responses = new ArrayList<>();
    private final Map<String, Response> responseMap = new HashMap<>();
    private ActionType actionType;
    private String description;
    private MappingDescriptionBuilder mappingDescription;
    private Resource resource;

    public static HttpActionBuilder httpAction() {
        return new HttpActionBuilder();
    }

    public static HttpActionBuilder defaultPostAction() {
        return httpAction()
                .withHttpActionType(POST)
                .withHttpActionOfDefaultRequestType();
    }

    public static HttpActionBuilder defaultPutAction() {
        return httpAction()
                .withHttpActionType(PUT)
                .withHttpActionOfDefaultRequestType();
    }

    public static HttpActionBuilder defaultPatchAction() {
        return httpAction()
                .withHttpActionType(PATCH)
                .withHttpActionOfDefaultRequestType();
    }

    public static HttpActionBuilder defaultDeleteAction() {
        return httpAction()
                .withHttpActionType(DELETE)
                .withHttpActionOfDefaultRequestType();
    }

    public static HttpActionBuilder defaultGetAction() {
        return httpAction()
                .withHttpActionType(GET)
                .withDefaultResponseType();
    }

    public static HttpActionBuilder httpActionWithDefaultMapping(final ActionType actionType, final String... mimeTypes) {
        HttpActionBuilder httpActionBuilder = new HttpActionBuilder()
                .withHttpActionType(actionType)
                .with(defaultMapping());
        for (final String mimeType : mimeTypes) {
            httpActionBuilder = httpActionBuilder.withMediaType(mimeType, "json/schema/some.schema.json");
        }
        return httpActionBuilder;
    }

    public static HttpActionBuilder httpAction(final ActionType actionType, final String... mimeTypes) {
        HttpActionBuilder httpActionBuilder = new HttpActionBuilder()
                .withHttpActionType(actionType);
        for (final String mimeType : mimeTypes) {
            httpActionBuilder = httpActionBuilder.withMediaType(mimeType, "");
        }
        return httpActionBuilder;
    }

    public HttpActionBuilder withHttpActionType(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public HttpActionBuilder withHttpActionOfDefaultRequestType() {
        return withMediaType("application/vnd.ctx.command.defcmd+json", "json/schema/ctx.command.defcmd.json")
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

    public HttpActionBuilder withHttpActionOfDefaultRequestAndResponseType() {
        return withMediaType("application/vnd.ctx.command.defcmd+json", "json/schema/ctx.command.defcmd.json")
                .withResponseTypes("application/vnd.ctx.query.defquery+json")
                .with(mapping()
                        .withName("action1")
                        .withRequestType("application/vnd.ctx.command.defcmd+json")
                        .withResponseType("application/vnd.ctx.query.defquery+json"));
    }

    public HttpActionBuilder withNullResponseType() {
        responses.add(null);
        return this;
    }

    public HttpActionBuilder withHttpActionResponseAndNoBody() {
        responses.add(new Response());
        return this;
    }

    public HttpActionBuilder withHttpActionResponseAndEmptyBody() {
        final Map<String, MimeType> respBody = new HashMap<>();
        final Response response = new Response();
        response.setBody(respBody);
        responses.add(response);
        return this;
    }

    public HttpActionBuilder withResponseTypes(final String... responseTypes) {
        responses.add(response().withBodyTypes(responseTypes).build());
        return this;
    }

    public HttpActionBuilder withResponsesFrom(final Map<String, Response> responses) {
        this.responseMap.putAll(responses);
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

    public HttpActionBuilder withMediaType(final MimeType mimeType, final Optional<String> schema) {
        schema.ifPresent(mimeType::setSchema);
        body.put(mimeType.toString(), mimeType);
        return this;
    }

    public HttpActionBuilder withMediaTypeWithoutSchema(final MimeType mimeType) {
        return withMediaType(mimeType, Optional.empty());
    }

    public HttpActionBuilder withMediaTypeWithoutSchema(final MimeTypeBuilder mimeType) {
        return withMediaType(mimeType.build(), Optional.empty());
    }

    public HttpActionBuilder withMediaType(final String stringMimeType, final Optional<String> schema) {
        return withMediaType(new MimeType(stringMimeType), schema);
    }

    public HttpActionBuilder withMediaType(final String stringMimeType, final String schema) {
        return withMediaType(new MimeType(stringMimeType), Optional.of(schema));
    }

    public HttpActionBuilder withMediaTypeWithoutSchema(final String stringMimeType) {
        return withMediaType(stringMimeType, Optional.empty());
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

    public HttpActionBuilder withResourceUri(final String uri) {
        resource = resource().withRelativeUri(uri).build();
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

        if (responseMap.isEmpty()) {
            final HashMap<String, Response> responsesFromList = new HashMap<>();
            responses.forEach(response -> responsesFromList.put(valueOf(OK.getStatusCode()), response));
            action.setResponses(responsesFromList);
        } else {
            action.setResponses(responseMap);
        }

        if (resource != null) {
            action.setResource(resource);
        }

        action.setQueryParameters(queryParameters);
        return action;
    }

}
