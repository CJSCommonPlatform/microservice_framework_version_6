package uk.gov.justice.services.clients.rest.generator;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.commons.helper.Names.nameFrom;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.rest.parameter.ParameterType;
import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.QueryParam;
import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.clients.rest.generator.strategy.ClientGenerationStrategy;
import uk.gov.justice.services.clients.rest.generator.strategy.ClientGenerationStrategyFactory;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.generators.commons.client.AbstractClientGenerator;
import uk.gov.justice.services.generators.commons.mapping.ActionMapping;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
import org.raml.model.Response;

/**
 * Generates code for a rest client.
 *
 * The generated client is a {@link ServiceComponent} with an additional {link @Remote} annotation.
 * The client will contain a method per media type within every httpAction, within every resource.
 */
public class RestClientGenerator extends AbstractClientGenerator {

    private static final String REST_CLIENT_HELPER = "restClientHelper";
    private static final String REST_CLIENT_PROCESSOR = "restClientProcessor";
    private static final int NUMBER_OF_PATH_SEGMENTS = 8;
    private static final int SERVICE_PATH_SEGMENT_INDEX = 7;
    private static final int PILLAR_PATH_SEGMENT_INDEX = 4;
    private static final int TIER_PATH_SEGMENT_INDEX = 5;

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
        return ImmutableList.of(restClientFieldProcessor(), restClientHelperField(), baseUriStaticFieldOf(raml));
    }

    @Override
    protected CodeBlock methodBodyOf(final Resource resource, final Action ramlAction, final MimeType mimeType) {
        final CodeBlock.Builder methodBody = CodeBlock.builder()
                .addStatement("final String path = \"$L\"", resource.getRelativeUri())
                .addStatement("final $T<$T> pathParams = $L.extractPathParametersFromPath(path)",
                        Set.class, String.class, REST_CLIENT_HELPER)
                .addStatement("final Set<QueryParam> queryParams = new $T<$T>()",
                        HashSet.class, QueryParam.class);

        ramlAction.getQueryParameters().forEach((name, queryParameter) ->
                methodBody.addStatement("queryParams.add(new QueryParam(\"$L\", $L, $T.$L))",
                        name, queryParameter.isRequired(), ParameterType.class, ParameterType.valueOf(queryParameter.getType()).name()));

        methodBody.addStatement("final $T def = new $T(BASE_URI, path, pathParams, queryParams, $S)",
                EndpointDefinition.class, EndpointDefinition.class, nameFrom(mimeType));


        switch (ramlAction.getType()) {
            case GET:
                methodBody.addStatement("return $L.get(def, envelope)", REST_CLIENT_PROCESSOR);
                break;
            case POST:
                methodBody.addStatement("$L.post(def, envelope)", REST_CLIENT_PROCESSOR);
                break;
            default:
                throw new IllegalArgumentException(format("Action %s not supported in REST client generator", ramlAction.getType().toString()));
        }
        return methodBody.build();
    }

    @Override
    protected TypeName methodReturnTypeOf(final Action ramlAction) {
        return ramlAction.getType().equals(GET) ? TypeName.get(JsonEnvelope.class) : TypeName.VOID;
    }

    @Override
    protected Stream<MimeType> mediaTypesOf(Action ramlAction) {
        switch (ramlAction.getType()) {
            case GET:
                final Response response = ramlAction.getResponses().get(valueOf(OK.getStatusCode()));
                if (response != null) {
                    return response.getBody().values().stream();
                } else {
                    return Stream.empty();
                }
            case POST:
                return ramlAction.getBody().values().stream();
            default:
                throw new IllegalStateException(format("Unsupported httpAction type %s", ramlAction.getType()));
        }
    }


    @Override
    protected String handlesAnnotationValueOf(final Action ramlAction, final MimeType mimeType, final GeneratorConfig generatorConfig) {
        final ClientGenerationStrategy generationStrategy = ClientGenerationStrategyFactory.createFrom(generatorConfig);
        final List<ActionMapping> actionMappings = generationStrategy.listOfActionMappings(ramlAction.getDescription());
        final Optional<ActionMapping> mapping = generationStrategy.mappingOf(actionMappings, mimeType, ramlAction.getType());
        return generationStrategy.handlesValue(mapping, nameFrom(mimeType));
    }

    private FieldSpec restClientHelperField() {
        return FieldSpec.builder(RestClientHelper.class, REST_CLIENT_HELPER)
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