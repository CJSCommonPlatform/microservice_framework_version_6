package uk.gov.justice.services.adapters.rest.generator;

import static com.squareup.javapoet.AnnotationSpec.builder;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.services.adapters.rest.generator.Generators.resourceImplementationNameOf;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionType;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionTypeWithRequestType;
import static uk.gov.justice.services.generators.commons.helper.Names.resourceMethodNameFrom;
import static uk.gov.justice.services.generators.commons.helper.Names.resourceMethodNameWithNoMimeTypeFrom;

import uk.gov.justice.services.adapter.rest.mapping.ActionMapper;
import uk.gov.justice.services.adapter.rest.mapping.ActionMapperHelper;
import uk.gov.justice.services.generators.commons.helper.RestResourceBaseUri;
import uk.gov.justice.services.generators.commons.mapping.ActionMapping;
import uk.gov.justice.services.generators.commons.mapping.ActionMappingParser;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.HttpHeaders;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

public class ActionMappingGenerator {

    private static final String ACTION_MAPPER_HELPER_FIELD = "actionMapperHelper";
    private static final String ACTION_MAPPER_CLASS_SUFFIX = "ActionMapper";
    private static final String METHOD_NAME_PARAMETER = "methodName";
    private static final String HTTP_METHOD_PARAMETER = "httpMethod";
    private static final String HEADERS_PARAMETER = "headers";

    public List<TypeSpec> generateFor(final Raml raml) {
        final Collection<Resource> resources = raml.getResources().values();
        return resources.stream()
                .map(resource -> generateActionMappingFor(resource, new RestResourceBaseUri(raml.getBaseUri())))
                .collect(toList());
    }

    private TypeSpec generateActionMappingFor(final Resource resource, final RestResourceBaseUri baseUri) {

        final String className = mapperClassNameOf(resource, baseUri);
        return classBuilder(className)
                .addModifiers(PUBLIC)
                .addSuperinterface(ActionMapper.class)
                .addAnnotation(builder(Named.class)
                        .addMember("value", "$S", className).build())
                .addField(FieldSpec.builder(ActionMapperHelper.class, ACTION_MAPPER_HELPER_FIELD, PRIVATE)
                        .build())
                .addMethod(constructorBuilder()
                        .addAnnotation(Inject.class)
                        .addModifiers(PUBLIC)
                        .addParameter(ActionMapperHelper.class, ACTION_MAPPER_HELPER_FIELD, FINAL)
                        .addCode(mapperConstructorCodeFor(resource))
                        .build())
                .addMethod(actionOfMethod())
                .build();
    }

    private CodeBlock mapperConstructorCodeFor(final Resource resource) {
        final CodeBlock.Builder constructorCode = CodeBlock.builder()
                .addStatement("this.$L = $L", ACTION_MAPPER_HELPER_FIELD, ACTION_MAPPER_HELPER_FIELD);

        //NOTE: there's a bit of ambiguity here: ramlActions (http methods) are not framework actions
        resource.getActions().values().forEach(ramlAction -> {
            final List<ActionMapping> actionMappings = new ActionMappingParser().listOf(ramlAction.getDescription());
            final ActionType actionType = ramlAction.getType();

            if (isSupportedActionType(actionType)) {

                actionMappings.forEach(actionMapping -> {
                    final String mediaType = actionMapping.mimeTypeFor(ramlAction.getType());

                    constructorCode.addStatement("$L.add($S, $S, $S)",
                            ACTION_MAPPER_HELPER_FIELD,
                            methodNameForAction(ramlAction, actionType, mediaType),
                            mediaType,
                            actionMapping.getName());
                });

            } else {
                throw new IllegalStateException(format("Http Method of type %s is not supported by the Action Mapper", actionType.toString()));
            }

        });
        return constructorCode.build();
    }

    private MethodSpec actionOfMethod() {
        return methodBuilder("actionOf")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(String.class, METHOD_NAME_PARAMETER, FINAL)
                .addParameter(String.class, HTTP_METHOD_PARAMETER, FINAL)
                .addParameter(HttpHeaders.class, HEADERS_PARAMETER, FINAL)
                .addStatement("return $L.actionOf($L, $L, $L)", ACTION_MAPPER_HELPER_FIELD, METHOD_NAME_PARAMETER, HTTP_METHOD_PARAMETER, HEADERS_PARAMETER)
                .returns(String.class)
                .build();
    }

    private String methodNameForAction(final Action ramlAction, final ActionType actionType, final String mediaType) {
        if (isSupportedActionTypeWithRequestType(actionType)) {
            return resourceMethodNameFrom(ramlAction, new MimeType(mediaType));
        } else {
            return resourceMethodNameWithNoMimeTypeFrom(ramlAction);
        }
    }

    private String mapperClassNameOf(final Resource resource, final RestResourceBaseUri baseUri) {
        return resourceImplementationNameOf(resource, baseUri) + ACTION_MAPPER_CLASS_SUFFIX;
    }

}
