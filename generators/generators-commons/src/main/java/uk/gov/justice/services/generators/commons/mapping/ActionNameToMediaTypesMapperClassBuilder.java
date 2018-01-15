package uk.gov.justice.services.generators.commons.mapping;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

import uk.gov.justice.services.core.annotation.MediaTypesMapper;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMapper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ActionNameToMediaTypesMapperClassBuilder {

    private static final String FIELD_NAME = "mappings";

    private final SchemaMappingClassNameGenerator schemaMappingClassNameGenerator;

    public ActionNameToMediaTypesMapperClassBuilder(final SchemaMappingClassNameGenerator schemaMappingClassNameGenerator) {
        this.schemaMappingClassNameGenerator = schemaMappingClassNameGenerator;
    }

    public TypeSpec generate(final List<ActionNameMapping> actionNameMappings, final String baseUri) {

        final String classSimpleName = schemaMappingClassNameGenerator.createMappingClassNameFrom(baseUri, ActionNameToMediaTypesMapper.class);

        return classBuilder(classSimpleName)
                .addModifiers(PUBLIC)
                .addAnnotation(MediaTypesMapper.class)
                .addSuperinterface(ClassName.get(ActionNameToMediaTypesMapper.class))
                .addMethod(constructor(actionNameMappings))
                .addField(field())
                .addMethod(getMapMethod())
                .build();
    }

    private FieldSpec field() {
        return FieldSpec.builder(stringToMediaTypesMap(), FIELD_NAME, PRIVATE, FINAL)
                .initializer("new $T<>()", HashMap.class)
                .build();
    }

    private MethodSpec constructor(final List<ActionNameMapping> actionNameMappings) {

        final Builder builder = constructorBuilder()
                .addModifiers(PUBLIC);

        actionNameMappings.forEach(actionNameMapping -> addStatement(builder, actionNameMapping));

        return builder.build();
    }

    private void addStatement(final Builder builder, final ActionNameMapping actionNameMapping) {

        builder.addCode(CodeBlock.builder().add("$N.put($S, new $T(",
                FIELD_NAME,
                actionNameMapping.name(),
                ClassName.get(MediaTypes.class)).build());

        builder.addCode(mediaType(actionNameMapping.requestType()));
        builder.addCode(CodeBlock.builder().add(", ").build());
        builder.addCode(mediaType(actionNameMapping.responseType()));

        builder.addCode("));\n");
    }

    private CodeBlock mediaType(final Optional<MediaType> mediaTypeOptional) {
        return mediaTypeOptional.map(mediaType -> CodeBlock.builder().add("new $T($S)", ClassName.get(MediaType.class), mediaType.toString()).build())
                .orElseGet(() -> CodeBlock.builder().add("null").build());
    }

    private MethodSpec getMapMethod() {
        return MethodSpec.methodBuilder("getActionNameToMediaTypesMap")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addStatement("return $N", FIELD_NAME)
                .returns(stringToMediaTypesMap())
                .build();
    }

    private TypeName stringToMediaTypesMap() {
        return ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(MediaTypes.class));
    }
}

