package uk.gov.justice.services.generators.commons.client;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.services.generators.commons.helper.Names.camelCase;
import static uk.gov.justice.services.generators.commons.helper.Names.nameFrom;

import uk.gov.justice.services.generators.commons.config.GeneratorProperties;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelper;
import uk.gov.justice.services.messaging.logging.LoggerUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClientGenerator implements Generator {

    protected static final String ENVELOPE = "envelope";

    @Override
    public void run(final Raml raml, final GeneratorConfig generatorConfig) {

        TypeSpec.Builder classSpec = classSpecOf(raml, generatorConfig)
                .addFields(fieldsOf(raml))
                .addMethods(methodsOf(raml, generatorConfig));

        writeToJavaFile(classSpec, generatorConfig);
    }

    private Stream<MethodSpec> methodsOf(final Resource resource, final GeneratorConfig generationConfig) {
        return resource.getActions().values().stream()
                .flatMap(ramlAction -> mediaTypesOf(ramlAction)
                        .map(mimeType ->
                                methodOf(
                                        resource,
                                        ramlAction,
                                        mimeType,
                                        generationConfig
                                )
                        ));
    }

    private MethodSpec methodOf(final Resource resource,
                                  final Action ramlAction,
                                  final MimeType mediaType,
                                  final GeneratorConfig generatorConfig) {

        final MethodSpec.Builder method = methodOf(ramlAction, mediaType, handlesAnnotationValueOf(ramlAction, mediaType, generatorConfig));

        method.addCode(methodBodyOf(resource, ramlAction, mediaType));
        method.returns(methodReturnTypeOf(ramlAction));
        return method.build();
    }

    private MethodSpec.Builder methodOf(final Action ramlAction, final MimeType mediaType, final String handlerValue) {
        final ClassName classLoggerUtils = ClassName.get(LoggerUtils.class);
        final ClassName classJsonEnvelopeLoggerHelper = ClassName.get(JsonEnvelopeLoggerHelper.class);


        return methodBuilder(methodNameOf(ramlAction.getType(), mediaType))
                .addModifiers(PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Handles.class)
                        .addMember("value", "$S", handlerValue)
                        .build())
                .addParameter(ParameterSpec.builder(JsonEnvelope.class, ENVELOPE)
                        .addModifiers(FINAL)
                        .build())
                .addStatement("$T.trace(LOGGER, () -> String.format(\"Handling remote request: %s\", $T.toEnvelopeTraceString(envelope)))",
                        classLoggerUtils, classJsonEnvelopeLoggerHelper);
    }

    protected String methodNameOf(final ActionType actionType, final MimeType mimeType) {
        final String actionTypeStr = actionType.name().toLowerCase();
        return camelCase(format("%s.%s", actionTypeStr, nameFrom(mimeType)));
    }


    private List<MethodSpec> methodsOf(final Raml raml, final GeneratorConfig generatorConfig) {
        return raml.getResources().values().stream()
                .flatMap(resource -> methodsOf(resource, generatorConfig))
                .collect(toList());
    }

    private TypeSpec.Builder classSpecOf(final Raml raml, final GeneratorConfig generatorConfig) {
        final String className = classNameOf(raml);
        return TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC, FINAL)
                .addAnnotation(Remote.class)
                .addAnnotation(AnnotationSpec.builder(ServiceComponent.class)
                        .addMember("value", "$T.$L", Component.class, GeneratorProperties.serviceComponentOf(generatorConfig))
                        .build())
                .addField(loggerConstantField(className));
    }


    private FieldSpec loggerConstantField(final String className) {
        final ClassName classLoggerFactory = ClassName.get(LoggerFactory.class);
        return FieldSpec.builder(Logger.class, "LOGGER")
                .addModifiers(PRIVATE, javax.lang.model.element.Modifier.STATIC, FINAL)
                .initializer(
                        CodeBlock.builder()
                                .add(format("$L.getLogger(%s.class)", className), classLoggerFactory).build()
                )
                .build();
    }

    private void writeToJavaFile(final TypeSpec.Builder classSpec, final GeneratorConfig generatorConfig) {
        try {
            JavaFile
                    .builder(generatorConfig.getBasePackageName(), classSpec.build())
                    .build()
                    .writeTo(generatorConfig.getOutputDirectory());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected abstract String classNameOf(final Raml raml);
    protected abstract Iterable<FieldSpec> fieldsOf(final Raml raml);
    protected abstract TypeName methodReturnTypeOf(Action ramlAction);
    protected abstract Stream<MimeType> mediaTypesOf(Action ramlAction);
    protected abstract CodeBlock methodBodyOf(Resource resource, Action ramlAction, MimeType mimeType);
    protected abstract String handlesAnnotationValueOf(Action ramlAction, MimeType mimeType, GeneratorConfig generatorConfig);

}
