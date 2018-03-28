package uk.gov.justice.services.generators.commons.mapping;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

import uk.gov.justice.services.core.annotation.SchemaIdMapper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.subscription.domain.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class SubscriptionMediaTypeToSchemaIdMapperClassBuilder {

    private static final String MEDIA_TYPE_PATTERN = "application/vnd.%s+json";
    private static final String FIELD_NAME = "mediaTypeToSchemaIdMap";

    private final SubscriptionSchemaMappingClassNameGenerator subscriptionSchemaMappingClassNameGenerator;

    public SubscriptionMediaTypeToSchemaIdMapperClassBuilder(final SubscriptionSchemaMappingClassNameGenerator subscriptionSchemaMappingClassNameGenerator) {
        this.subscriptionSchemaMappingClassNameGenerator = subscriptionSchemaMappingClassNameGenerator;
    }

    public TypeSpec typeSpecWith(
            final String contextName,
            final String componentName,
            final List<Event> events) {

        final String classSimpleName = subscriptionSchemaMappingClassNameGenerator.createMappingClassNameFrom(
                contextName,
                componentName,
                MediaTypeToSchemaIdMapper.class);

        return classBuilder(classSimpleName)
                .addModifiers(PUBLIC)
                .addAnnotation(SchemaIdMapper.class)
                .addSuperinterface(ClassName.get(MediaTypeToSchemaIdMapper.class))
                .addMethod(constructor(events))
                .addField(field())
                .addMethod(getMapMethod())
                .build();
    }

    private FieldSpec field() {
        return FieldSpec.builder(mediaTypeToSchemaIdMap(), FIELD_NAME, PRIVATE, FINAL)
                .initializer("new $T<>()", HashMap.class)
                .build();
    }

    private MethodSpec constructor(final List<Event> events) {

        final MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC);

        events.forEach(event ->
                builder.addStatement(
                        "$N.put(new $T($S), $S)",
                        FIELD_NAME,
                        MediaType.class,
                        toMediaType(event.getName()),
                        event.getSchemaUri()
                )
        );

        return builder.build();
    }

    private MethodSpec getMapMethod() {
        return MethodSpec.methodBuilder("getMediaTypeToSchemaIdMap")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addStatement("return $N", FIELD_NAME)
                .returns(mediaTypeToSchemaIdMap())
                .build();
    }

    private TypeName mediaTypeToSchemaIdMap() {
        return ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(MediaType.class),
                ClassName.get(String.class));
    }

    private String toMediaType(final String eventName) {
        return format(MEDIA_TYPE_PATTERN, eventName);
    }
}
