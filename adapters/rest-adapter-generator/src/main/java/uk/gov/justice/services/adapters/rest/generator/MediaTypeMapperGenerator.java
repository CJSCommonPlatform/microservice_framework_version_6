package uk.gov.justice.services.adapters.rest.generator;

import static com.squareup.javapoet.AnnotationSpec.builder;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.raml.common.generator.Names.buildResourceMethodName;
import static uk.gov.justice.raml.common.generator.Names.buildResourceMethodNameWithNoMimeType;
import static uk.gov.justice.raml.common.generator.Names.mapperClassNameOf;
import static uk.gov.justice.raml.common.generator.Names.nameFrom;
import static uk.gov.justice.services.adapters.rest.generator.Actions.responseMimeTypesOf;
import static uk.gov.justice.services.adapters.rest.generator.Generators.byMimeTypeOrder;

import uk.gov.justice.services.adapter.rest.BasicActionMapper;

import java.util.Collection;
import java.util.function.BiFunction;

import javax.inject.Named;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public class MediaTypeMapperGenerator extends AbstractInternalGenerator {

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

        resource.getActions().values().forEach(ramlAction -> {
            constructorCode.add(statementsOf(ramlAction));
        });

        return constructorCode.build();
    }

    /**
     * Process the body or bodies for each httpAction.
     *
     * @param ramlAction the httpAction to statementsOf
     * @return the list of {@link CodeBlock} that represents each method for the httpAction
     */
    private CodeBlock statementsOf(final Action ramlAction) {
        if (!ramlAction.hasBody()) {
            return processActionBody(ramlAction, responseMimeTypesOf(ramlAction), this::resourceMethodNameSingle);
        } else {
            return processActionBody(ramlAction, requestMimeTypes(ramlAction), this::resourceMethodNameMultiple);
        }
    }

    private CodeBlock processActionBody(final Action ramlAction, final Collection<MimeType> mimeTypes,
                                        final BiFunction<Action, MimeType, String> resourceMethodName) {
        final CodeBlock.Builder codeBlock = CodeBlock.builder();

        mimeTypes.stream()
                .sorted(byMimeTypeOrder())
                .forEach(mimeType ->
                        codeBlock.addStatement("add($S, $S, $S)",
                                resourceMethodName.apply(ramlAction, mimeType),
                                mimeType.getType(),
                                nameFrom(mimeType))
                );

        return codeBlock.build();
    }

    private String resourceMethodNameMultiple(final Action ramlAction, final MimeType mimeType) {
        return buildResourceMethodName(ramlAction, mimeType);
    }

    private String resourceMethodNameSingle(final Action ramlAction, final MimeType mimeType) {
        return buildResourceMethodNameWithNoMimeType(ramlAction);
    }

    private Collection<MimeType> requestMimeTypes(final Action ramlAction) {
        return ramlAction.getBody().values();
    }

}
