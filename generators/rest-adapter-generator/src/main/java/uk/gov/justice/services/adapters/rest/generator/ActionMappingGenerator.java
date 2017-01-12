package uk.gov.justice.services.adapters.rest.generator;

import static com.squareup.javapoet.AnnotationSpec.builder;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionType;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionTypeWithRequestType;
import static uk.gov.justice.services.generators.commons.helper.Names.buildResourceMethodName;
import static uk.gov.justice.services.generators.commons.helper.Names.buildResourceMethodNameWithNoMimeType;
import static uk.gov.justice.services.generators.commons.helper.Names.mapperClassNameOf;

import uk.gov.justice.services.adapter.rest.BasicActionMapper;
import uk.gov.justice.services.generators.commons.mapping.ActionMapping;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

public class ActionMappingGenerator {

    public List<TypeSpec> generateFor(final Raml raml) {
        final Collection<Resource> resources = raml.getResources().values();
        return resources.stream()
                .map(this::generateActionMappingFor)
                .collect(toList());
    }

    private TypeSpec generateActionMappingFor(final Resource resource) {

        final String className = mapperClassNameOf(resource);
        return classBuilder(className)
                .addModifiers(PUBLIC)
                .superclass(ClassName.get(BasicActionMapper.class))
                .addAnnotation(builder(Named.class)
                        .addMember("value", "$S", className).build())
                .addMethod(constructorBuilder()
                        .addModifiers(PUBLIC)
                        .addCode(mapperConstructorCodeFor(resource))
                        .build())
                .build();
    }

    private CodeBlock mapperConstructorCodeFor(final Resource resource) {
        final CodeBlock.Builder constructorCode = CodeBlock.builder();

        //NOTE: there's a bit of ambiguity here: ramlActions (http methods) are not framework actions
        resource.getActions().values().forEach(ramlAction -> {
            final List<ActionMapping> actionMappings = ActionMapping.listOf(ramlAction.getDescription());
            final ActionType actionType = ramlAction.getType();

            if (isSupportedActionType(actionType)) {

                actionMappings.forEach(actionMapping -> {
                    final String mediaType = actionMapping.mimeTypeFor(ramlAction.getType());

                    constructorCode.addStatement("add($S, $S, $S)",
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

    private String methodNameForAction(final Action ramlAction, final ActionType actionType, final String mediaType) {
        if (isSupportedActionTypeWithRequestType(actionType)) {
            return buildResourceMethodName(ramlAction, new MimeType(mediaType));
        } else {
            return buildResourceMethodNameWithNoMimeType(ramlAction);
        }
    }
}
