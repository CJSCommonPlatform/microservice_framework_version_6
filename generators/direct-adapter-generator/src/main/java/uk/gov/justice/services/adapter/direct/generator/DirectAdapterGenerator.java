package uk.gov.justice.services.adapter.direct.generator;


import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.direct.DefaultDirectAdapterProcessor;
import uk.gov.justice.services.adapter.direct.DirectAdapterProcessor;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.generators.commons.helper.RestResourceBaseUri;
import uk.gov.justice.services.generators.commons.mapping.ActionMapping;
import uk.gov.justice.services.generators.commons.validator.ActionMappingRamlValidator;
import uk.gov.justice.services.generators.commons.validator.CompositeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsActionsRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.services.generators.commons.validator.ResponseContentTypeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.SupportedActionTypesRamlValidator;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectAdapterGenerator implements Generator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectAdapterGenerator.class);
    private static final String INTERCEPTOR_CHAIN_PROCESSOR_FIELD = "interceptorChainProcessor";
    private static final String DIRECT_ADAPTER_PROCESSOR_FIELD = "directAdapterProcessor";

    private final RamlValidator validator = new CompositeRamlValidator(
            new ContainsResourcesRamlValidator(),
            new ContainsActionsRamlValidator(),
            new SupportedActionTypesRamlValidator(GET),
            new ResponseContentTypeRamlValidator(GET),
            new ActionMappingRamlValidator()
    );

    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {
        validator.validate(raml);
        raml.getResources().values().stream()
                .map(resource -> generatedClassesFrom(resource, new RestResourceBaseUri(raml.getBaseUri())))
                .forEach(generatedClass -> writeClass(configuration, configuration.getBasePackageName(), generatedClass, LOGGER));
    }

    private TypeSpec generatedClassesFrom(final Resource resource, final RestResourceBaseUri baseUri) {

        final String actionsArrayLiteral = actionsArrayLiteral(resource);
        return classBuilder(adapterClassNameFrom(resource, baseUri))
                .addModifiers(PUBLIC)
                .addSuperinterface(SynchronousDirectAdapter.class)
                .addAnnotation(AnnotationSpec.builder(DirectAdapter.class)
                        .addMember("component", "$S", baseUri.component().get())
                        .addMember("actions", "$L", actionsArrayLiteral)
                        .build())
                .addField(FieldSpec.builder(InterceptorChainProcessor.class, INTERCEPTOR_CHAIN_PROCESSOR_FIELD)
                        .addAnnotation(Inject.class)
                        .build())
                .addField(FieldSpec.builder(DirectAdapterProcessor.class, DIRECT_ADAPTER_PROCESSOR_FIELD)
                        .initializer("new $T(new String[]$L)", DefaultDirectAdapterProcessor.class, actionsArrayLiteral)
                        .build())
                .addMethods(singletonList(method())).build();
    }

    private MethodSpec method() {
        return MethodSpec.methodBuilder("process")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(ParameterSpec.builder(JsonEnvelope.class, "envelope").build())
                .returns(JsonEnvelope.class)
                .addCode(CodeBlock.builder()
                        .addStatement("return $L.process(envelope, $L::process)", DIRECT_ADAPTER_PROCESSOR_FIELD, INTERCEPTOR_CHAIN_PROCESSOR_FIELD).build())
                .build();
    }

    private String actionsArrayLiteral(final Resource resource) {
        final String delimitedActions = resource.getActions().values().stream()
                .flatMap(ramlAction -> ActionMapping.listOf(ramlAction.getDescription()).stream())
                .map(actionMapping -> actionMapping.getName())
                .collect(joining("\",\""));
        return format("{\"%s\"}", delimitedActions);
    }

    private String adapterClassNameFrom(final Resource resource, final RestResourceBaseUri baseUri) {
        return buildJavaFriendlyName(format("%s%sDirectAdapter", baseUri.classNamePrefix(), resource.getRelativeUri()));
    }
}
