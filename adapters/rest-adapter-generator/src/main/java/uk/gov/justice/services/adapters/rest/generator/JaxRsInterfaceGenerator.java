package uk.gov.justice.services.adapters.rest.generator;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.strip;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.rest.generator.Actions.responseMimeTypesOf;
import static uk.gov.justice.services.adapters.rest.generator.Generators.byMimeTypeOrder;
import static uk.gov.justice.services.adapters.rest.generator.Names.DEFAULT_ANNOTATION_PARAMETER;
import static uk.gov.justice.services.adapters.rest.generator.Names.GENERIC_PAYLOAD_ARGUMENT_NAME;
import static uk.gov.justice.services.adapters.rest.generator.Names.buildResourceMethodName;
import static uk.gov.justice.services.adapters.rest.generator.Names.buildResourceMethodNameWithNoMimeType;
import static uk.gov.justice.services.adapters.rest.generator.Names.resourceInterfaceNameOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;

/**
 * Internal code generation class for generating the JAX-RS interface.
 */
class JaxRsInterfaceGenerator {

    private static final String ANNOTATION_FORMAT = "$S";

    /**
     * Generate a JaxRs interface for each resource.
     *
     * @param resources the collection of {@link Resource} to generate as implementation classes
     * @return a list of {@link TypeSpec} that represent the implementation classes
     */
    List<TypeSpec> generateFor(final Collection<Resource> resources) {
        return resources.stream()
                .map(this::createInterfaceFor)
                .collect(Collectors.toList());
    }

    /**
     * Create an interface for the specified {@link Resource}
     *
     * @param resource the resource to generate as an implementation class
     * @return a {@link TypeSpec} that represents the implementation class
     */
    private TypeSpec createInterfaceFor(final Resource resource) {
        final TypeSpec.Builder interfaceSpecBuilder = interfaceSpecFor(resource);

        resource.getActions().values().forEach(action ->
                interfaceSpecBuilder.addMethods(forEach(action)));

        return interfaceSpecBuilder.build();
    }

    /**
     * Process the body or bodies for each action.
     *
     * @param action the action to forEach
     * @return the list of {@link MethodSpec} that represents each method for the action
     */
    private List<MethodSpec> forEach(final Action action) {
        final Collection<MimeType> responseMimeTypes = responseMimeTypesOf(action);

        if (!action.hasBody()) {
            return Collections.singletonList(processNoActionBody(action, responseMimeTypes));
        } else {
            return processOneOrMoreActionBodies(action, responseMimeTypes);
        }
    }

    /**
     * Process an action with no body.
     *
     * @param action the action to process
     * @return the {@link MethodSpec} that represents a method for the action
     */
    private MethodSpec processNoActionBody(final Action action,
                                           final Collection<MimeType> responseMimeTypes) {
        final String resourceMethodName = buildResourceMethodNameWithNoMimeType(action);
        return generateResourceMethod(action, resourceMethodName, responseMimeTypes).build();
    }

    /**
     * Process an action with one or more bodies.
     *
     * @param action the action to process
     * @return the list of {@link MethodSpec} that represents each method for the action
     */
    private List<MethodSpec> processOneOrMoreActionBodies(final Action action,
                                                          final Collection<MimeType> responseMimeTypes) {
        return action.getBody().values().stream()
                .sorted(byMimeTypeOrder())
                .map(bodyMimeType -> {
                    final String resourceMethodName = buildResourceMethodName(action, bodyMimeType);
                    final MethodSpec.Builder methodBuilder = generateResourceMethod(action, resourceMethodName, responseMimeTypes);
                    return addToMethodWithMimeType(methodBuilder, bodyMimeType).build();
                }).collect(Collectors.toList());
    }

    /**
     * Creates a {@link TypeSpec.Builder} from an initial template of an interface
     *
     * @param resource the resource to generate as an interface
     * @return a {@link TypeSpec.Builder} that represents the interface
     */
    private TypeSpec.Builder interfaceSpecFor(final Resource resource) {
        return interfaceBuilder(resourceInterfaceNameOf(resource))
                .addModifiers(PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Path.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, ANNOTATION_FORMAT, pathAnnotationFor(resource))
                        .build());
    }

    /**
     * Generate path annotation for a resource.
     *
     * @param resource generate for this resource
     * @return the path annotation string
     */
    private String pathAnnotationFor(final Resource resource) {
        return defaultIfBlank(strip(resource.getRelativeUri(), "/"), "/");
    }

    /**
     * Add MimeType specific annotation and parameter to method.
     *
     * @param methodBuilder add annotation and parameter to this method builder
     * @return the method builder
     */
    private MethodSpec.Builder addToMethodWithMimeType(final MethodSpec.Builder methodBuilder,
                                                       final MimeType bodyMimeType) {
        return methodBuilder
                .addAnnotation(AnnotationSpec.builder(Consumes.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, ANNOTATION_FORMAT, bodyMimeType.getType())
                        .build())
                .addParameter(ParameterSpec
                        .builder(JsonObject.class, GENERIC_PAYLOAD_ARGUMENT_NAME)
                        .build());
    }

    /**
     * Generate a method for each {@link Action}.
     *
     * @param action             the action to generate as a method
     * @param resourceMethodName the resource method name to generate
     * @return a {@link MethodSpec} that represents the generated method
     * @throws IllegalStateException if action type is not GET or POST
     */
    private MethodSpec.Builder generateResourceMethod(final Action action,
                                                      final String resourceMethodName,
                                                      final Collection<MimeType> responseMimeTypes) {

        final ActionType actionType = action.getType();
        final Map<String, QueryParameter> queryParams = action.getQueryParameters();
        final Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        final AnnotationSpec actionTypeAnnotation;

        if (actionType == GET) {
            actionTypeAnnotation = AnnotationSpec.builder(javax.ws.rs.GET.class).build();
        } else if (actionType == POST) {
            actionTypeAnnotation = AnnotationSpec.builder(javax.ws.rs.POST.class).build();
        } else {
            throw new IllegalStateException(String.format("Unsupported action type %s", actionType));
        }

        return methodBuilder(resourceMethodName)
                .addModifiers(PUBLIC, ABSTRACT)
                .addAnnotation(actionTypeAnnotation)
                .addAnnotations(annotationsForProduces(responseMimeTypes))
                .addParameters(methodPathParams(pathParams.keySet()))
                .addParameters(methodQueryParams(queryParams.keySet()))
                .returns(Response.class);
    }

    /**
     * Generate code to add all query parameters to the params map.
     *
     * @param queryParams the query param names to add to the map
     * @return the {@link CodeBlock} that represents the generated code
     */
    private List<ParameterSpec> methodQueryParams(final Set<String> queryParams) {
        return methodParams(queryParams, QueryParam.class);
    }

    /**
     * Generate method parameters for all the path params.
     *
     * @param pathParams the path param names to generate
     * @return list of {@link ParameterSpec} that represent the method parameters
     */
    private List<ParameterSpec> methodPathParams(final Set<String> pathParams) {
        return methodParams(pathParams, PathParam.class);
    }

    /**
     * Generate code to add all parameters to the params map.
     *
     * @param params the param names to add to the map
     * @return the {@link CodeBlock} that represents the generated code
     */
    private List<ParameterSpec> methodParams(final Collection<String> params, final Class<?> paramAnnotationClass) {
        return params.stream().map(name ->
                ParameterSpec
                        .builder(String.class, name)
                        .addAnnotation(AnnotationSpec.builder(paramAnnotationClass)
                                .addMember(DEFAULT_ANNOTATION_PARAMETER, ANNOTATION_FORMAT, name)
                                .build())
                        .build()
        ).collect(Collectors.toList());
    }

    /**
     * Generates the Produces annotation for all the response mime types.
     *
     * @param responseMimeTypes generate annotations for each responseMimeType
     * @return the list of {@link AnnotationSpec} that represent the produces annotations
     */
    private List<AnnotationSpec> annotationsForProduces(final Collection<MimeType> responseMimeTypes) {
        final List<AnnotationSpec> specs = new ArrayList<>();

        if (!responseMimeTypes.isEmpty()) {
            final AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(Produces.class);

            responseMimeTypes.stream().forEach(responseMimeType ->
                    annotationBuilder.addMember(DEFAULT_ANNOTATION_PARAMETER, ANNOTATION_FORMAT, responseMimeType.getType()));

            specs.add(annotationBuilder.build());
        }

        return specs;
    }

}
