package uk.gov.justice.services.generators.commons.client;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition.definitionWithRequest;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition.definitionWithRequestAndResponse;
import static uk.gov.justice.services.generators.commons.config.GeneratorProperties.serviceComponentOf;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionType;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionTypeWithRequestType;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSynchronousAction;
import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.services.generators.commons.helper.Names.buildResourceMethodNameFromVerbUriAndMimeType;
import static uk.gov.justice.services.generators.commons.helper.Names.camelCase;
import static uk.gov.justice.services.generators.commons.helper.Names.nameFrom;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.generators.commons.validator.CompositeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClientGenerator implements Generator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientGenerator.class);
    protected static final String ENVELOPE = "envelope";
    private static final String OK = "200";
    private static final String TRACE_LOGGER_FIELD = "traceLogger";

    @Override
    public void run(final Raml raml, final GeneratorConfig generatorConfig) {

        validator().validate(raml);

        final TypeSpec.Builder classSpec = classSpecOf(raml, generatorConfig)
                .addFields(fieldsOf(raml))
                .addField(FieldSpec.builder(TraceLogger.class, TRACE_LOGGER_FIELD)
                        .addAnnotation(Inject.class)
                        .build())
                .addMethods(methodsOf(raml, generatorConfig));
        writeClass(generatorConfig, generatorConfig.getBasePackageName(), classSpec.build(), LOGGER);
    }

    protected RamlValidator validator() {
        return raml -> {
        };
    }

    protected abstract String classNameOf(final Raml raml);

    protected abstract Iterable<FieldSpec> fieldsOf(final Raml raml);

    protected abstract TypeName methodReturnTypeOf(Action ramlAction);

    protected abstract CodeBlock methodBodyOf(Resource resource, Action ramlAction, ActionMimeTypeDefinition definition);

    protected abstract String handlesAnnotationValueOf(Action ramlAction, ActionMimeTypeDefinition definition, GeneratorConfig generatorConfig);

    protected Class<?> classAnnotation() {
        return Remote.class;
    }

    private Stream<MethodSpec> methodsOf(final Resource resource, final GeneratorConfig generationConfig) {
        return resource.getActions().values().stream()
                .flatMap(ramlAction -> mimeTypesOf(ramlAction)
                        .filter(this::supportedMimeType)
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
                                final ActionMimeTypeDefinition definition,
                                final GeneratorConfig generatorConfig) {

        final MethodSpec.Builder method = methodOf(ramlAction, definition, handlesAnnotationValueOf(ramlAction, definition, generatorConfig));

        method.addCode(methodBodyOf(resource, ramlAction, definition));
        method.returns(methodReturnTypeOf(ramlAction));
        return method.build();
    }

    private MethodSpec.Builder methodOf(final Action ramlAction, final ActionMimeTypeDefinition definition, final String handlerValue) {
        final String methodName = buildResourceMethodNameFromVerbUriAndMimeType(ramlAction, definition);
        return methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Handles.class)
                        .addMember("value", "$S", handlerValue)
                        .build())
                .addParameter(ParameterSpec.builder(JsonEnvelope.class, ENVELOPE)
                        .addModifiers(FINAL)
                        .build())
                .addStatement("$L.trace(LOGGER, () -> String.format(\"Handling remote request: %s\", envelope))",
                        TRACE_LOGGER_FIELD);
    }

    private Stream<ActionMimeTypeDefinition> mimeTypesOf(final Action ramlAction) {
        final ActionType actionType = ramlAction.getType();

        if (isSupportedActionType(actionType)) {
            if (isSupportedActionTypeWithRequestType(actionType)) {
                return actionMimeTypesForRequestAndResponseOf(ramlAction);
            } else {
                return responseMediaTypesOf(ramlAction).map(ActionMimeTypeDefinition::definitionWithResponse);
            }
        }

        throw new IllegalStateException(format("Unsupported httpAction type %s", actionType));
    }

    private Stream<ActionMimeTypeDefinition> actionMimeTypesForRequestAndResponseOf(final Action ramlAction) {
        return ramlAction.getBody().values().stream()
                .flatMap(responseType -> {
                    if (isSynchronousAction(ramlAction)) {
                        return responseMediaTypesOf(ramlAction)
                                .map(requestType -> definitionWithRequestAndResponse(responseType, requestType));
                    } else {
                        return Stream.of(definitionWithRequest(responseType));
                    }
                });
    }

    private Stream<MimeType> responseMediaTypesOf(final Action ramlAction) {
        final Response response = ramlAction.getResponses().get(OK);
        if (response != null) {
            return response.getBody().values().stream();
        } else {
            return Stream.empty();
        }
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
                .addAnnotation(classAnnotation())
                .addAnnotation(AnnotationSpec.builder(FrameworkComponent.class)
                        .addMember("value", "$S", serviceComponentOf(generatorConfig))
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

    private boolean supportedMimeType(final ActionMimeTypeDefinition mimeType) {
        final String mimeTypeStr = mimeType.getNameType().getType();
        return mimeTypeStr.startsWith("application/vnd.") && mimeTypeStr.endsWith("+json");
    }
}
