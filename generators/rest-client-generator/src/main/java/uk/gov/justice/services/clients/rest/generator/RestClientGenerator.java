package uk.gov.justice.services.clients.rest.generator;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.justice.services.generators.commons.helper.Actions.hasResponseMimeTypes;
import static uk.gov.justice.services.generators.commons.helper.Names.nameFrom;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.INVALID_ACTION_MAPPING_ERROR_MSG;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.QueryParam;
import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.generators.commons.client.AbstractClientGenerator;
import uk.gov.justice.services.generators.commons.client.ActionMimeTypes;
import uk.gov.justice.services.generators.commons.mapping.ActionMapping;
import uk.gov.justice.services.generators.commons.validator.RamlValidationException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.rest.ParameterType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

/**
 * Generates code for a rest client.
 *
 * The generated client is a {@link ServiceComponent} with an additional {link @Remote} annotation.
 * The client will contain a method per media type within every httpAction, within every resource.
 */
public class RestClientGenerator extends AbstractClientGenerator {

    private static final String REST_CLIENT_HELPER = "restClientHelper";
    private static final String REST_CLIENT_PROCESSOR = "restClientProcessor";
    private static final String ENVELOPER = "enveloper";
    private static final String OUTPUT_ENVELOPE = "outputEnvelope";

    private static final int NUMBER_OF_PATH_SEGMENTS = 8;
    private static final int SERVICE_PATH_SEGMENT_INDEX = 7;
    private static final int PILLAR_PATH_SEGMENT_INDEX = 4;
    private static final int TIER_PATH_SEGMENT_INDEX = 5;

    private static final String SYNC_GET_RETURN_STATEMENT = "return $L.get(def, envelope)";

    private static final String ASYNC_POST_STATEMENT = "$L.post(def, $L)";
    private static final String SYNC_POST_RETURN_STATEMENT = "return $L.synchronousPost(def, $L)";

    private static final String ASYNC_PUT_STATEMENT = "$L.put(def, $L)";
    private static final String SYNC_PUT_RETURN_STATEMENT = "return $L.synchronousPut(def, $L)";

    private static final String ASYNC_PATCH_STATEMENT = "$L.patch(def, $L)";
    private static final String SYNC_PATCH_RETURN_STATEMENT = "return $L.synchronousPatch(def, $L)";

    @Override
    protected String classNameOf(final Raml raml) {
        final String[] pathSegments = raml.getBaseUri().split("/");
        if (pathSegments.length != NUMBER_OF_PATH_SEGMENTS) {
            throw new IllegalArgumentException("baseUri must have 8 parts");
        }
        return format("Remote%s%s%s",
                capitalize(pathSegments[SERVICE_PATH_SEGMENT_INDEX]),
                capitalize(pathSegments[PILLAR_PATH_SEGMENT_INDEX]),
                capitalize(pathSegments[TIER_PATH_SEGMENT_INDEX]));
    }

    @Override
    protected Iterable<FieldSpec> fieldsOf(final Raml raml) {
        return ImmutableList.of(
                restClientFieldProcessor(),
                restClientHelperField(),
                baseUriStaticFieldOf(raml),
                enveloperField()
        );
    }

    @Override
    protected CodeBlock methodBodyOf(final Resource resource, final Action ramlAction, final ActionMimeTypes mimeTypes) {
        final String actionName = nameFrom(mimeTypes.getNameType());
        final String responseName = nameFrom(mimeTypes.getResponseType());

        final CodeBlock.Builder methodBody = CodeBlock.builder()
                .addStatement("final String path = \"$L\"", resource.getRelativeUri())
                .addStatement("final $T<$T> pathParams = $L.extractPathParametersFromPath(path)",
                        Set.class, String.class, REST_CLIENT_HELPER)
                .addStatement("final Set<QueryParam> queryParams = new $T<$T>()",
                        HashSet.class, QueryParam.class);

        ramlAction.getQueryParameters().forEach((name, queryParameter) ->
                methodBody.addStatement("queryParams.add(new QueryParam(\"$L\", $L, $T.$L))",
                        name, queryParameter.isRequired(), ParameterType.class, ParameterType.valueOfQueryType(queryParameter.getType().name()).name()));

        methodBody.addStatement("final $T def = new $T(BASE_URI, path, pathParams, queryParams, $S)",
                EndpointDefinition.class, EndpointDefinition.class, responseName);

        switch (ramlAction.getType()) {
            case GET:
                methodBody.addStatement(SYNC_GET_RETURN_STATEMENT, REST_CLIENT_PROCESSOR);
                break;

            case POST:
                methodBody.add(
                        methodStatementsFrom(
                                actionName,
                                hasResponseMimeTypes(ramlAction) ? SYNC_POST_RETURN_STATEMENT : ASYNC_POST_STATEMENT
                        ));

                break;

            case PUT:
                methodBody.add(
                        methodStatementsFrom(
                                actionName,
                                hasResponseMimeTypes(ramlAction) ? SYNC_PUT_RETURN_STATEMENT : ASYNC_PUT_STATEMENT
                        ));
                break;

            case PATCH:
                methodBody.add(
                        methodStatementsFrom(
                                actionName,
                                hasResponseMimeTypes(ramlAction) ? SYNC_PATCH_RETURN_STATEMENT : ASYNC_PATCH_STATEMENT
                        ));
                break;

            default:
                throw new IllegalArgumentException(format("Action %s not supported in REST client generator", ramlAction.getType().toString()));
        }

        return methodBody.build();
    }

    @Override
    protected TypeName methodReturnTypeOf(final Action ramlAction) {
        return hasResponseMimeTypes(ramlAction) ? TypeName.get(JsonEnvelope.class) : TypeName.VOID;
    }

    @Override
    protected String handlesAnnotationValueOf(final Action ramlAction, final ActionMimeTypes mimeTypes, final GeneratorConfig generatorConfig) {
        return actionMappingOf(ramlAction, mimeTypes.getNameType())
                .orElseThrow(() -> new RamlValidationException(INVALID_ACTION_MAPPING_ERROR_MSG))
                .getName();
    }

    private CodeBlock methodStatementsFrom(final String actionName, final String statementTemplate) {
        return CodeBlock.builder()
                .addStatement("final JsonEnvelope $L = $L.withMetadataFrom(envelope, $S).apply(envelope.payload())", OUTPUT_ENVELOPE, ENVELOPER, actionName)
                .addStatement(statementTemplate, REST_CLIENT_PROCESSOR, OUTPUT_ENVELOPE)
                .build();
    }

    private Optional<ActionMapping> actionMappingOf(final Action ramlAction, final MimeType mimeType) {
        final List<ActionMapping> actionMappings = ActionMapping.listOf(ramlAction.getDescription());
        return actionMappings.stream().filter(m -> m.mimeTypeFor(ramlAction.getType()).equals(mimeType.getType())).findAny();
    }

    private FieldSpec restClientHelperField() {
        return FieldSpec.builder(RestClientHelper.class, REST_CLIENT_HELPER)
                .addAnnotation(Inject.class)
                .build();
    }

    private FieldSpec enveloperField() {
        return FieldSpec.builder(Enveloper.class, ENVELOPER)
                .addAnnotation(Inject.class)
                .build();
    }

    private FieldSpec restClientFieldProcessor() {
        return FieldSpec.builder(RestClientProcessor.class, REST_CLIENT_PROCESSOR)
                .addAnnotation(Inject.class)
                .build();
    }

    private FieldSpec baseUriStaticFieldOf(final Raml raml) {
        return FieldSpec.builder(String.class, "BASE_URI")
                .addModifiers(Modifier.PRIVATE, FINAL, Modifier.STATIC)
                .initializer("$S", raml.getBaseUri())
                .build();
    }
}