package uk.gov.justice.services.adapters.rest.generator;

import static com.squareup.javapoet.AnnotationSpec.builder;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.commons.helper.Names.buildResourceMethodName;
import static uk.gov.justice.services.generators.commons.helper.Names.mapperClassNameOf;

import uk.gov.justice.services.adapter.rest.BasicActionMapper;
import uk.gov.justice.services.generators.commons.mapping.ActionMapping;

import java.util.List;

import javax.inject.Named;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.MimeType;
import org.raml.model.Resource;

/**
 * Generates a mapper class that maps media types to framework actions
 */
public class ActionMapperGenerator extends AbstractInternalGenerator {

    @Override
    TypeSpec generateFor(final Resource resource) {

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
            actionMappings.forEach(m -> {
                final String mediaType = m.mimeTypeFor(ramlAction.getType());
                constructorCode.addStatement("add($S, $S, $S)",
                        buildResourceMethodName(ramlAction, ramlAction.getType() == POST ? new MimeType(mediaType) : null),
                        mediaType,
                        m.getName());
            });

        });
        return constructorCode.build();
    }

}