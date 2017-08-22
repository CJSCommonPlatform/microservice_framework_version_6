package uk.gov.justice.services.clients.direct.generator;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapterCache;
import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.generators.commons.client.AbstractClientGenerator;
import uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition;
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
import com.squareup.javapoet.TypeName;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;


public class DirectClientGenerator extends AbstractClientGenerator {

    private static final String ADAPTER_CACHE_FIELD = "adapterCache";
    private static final String COMPONENT_NAME_VARIABLE = "componentName";

    @Override
    protected RamlValidator validator() {
        return new CompositeRamlValidator(
                new ContainsResourcesRamlValidator(),
                new ContainsActionsRamlValidator(),
                new SupportedActionTypesRamlValidator(GET),
                new ResponseContentTypeRamlValidator(GET),
                new ActionMappingRamlValidator()
        );
    }

    @Override
    protected String classNameOf(final Raml raml, final String serviceComponent) {
        return buildJavaFriendlyName(format("Direct_%s2%sClient", serviceComponent.toLowerCase(), new RestResourceBaseUri(raml.getBaseUri()).pathWithoutWebContext()));
    }

    @Override
    protected AnnotationSpec classAnnotation(final Raml raml) {
        return AnnotationSpec.builder(Direct.class).addMember("target", "$S",
                new RestResourceBaseUri(raml.getBaseUri()).component()
                        .orElseThrow(() -> new IllegalArgumentException("Target component could not be derived from RAML")))
                .build();
    }

    @Override
    protected Iterable<FieldSpec> fieldsOf(final Raml raml) {
        return asList(
                FieldSpec.builder(String.class, COMPONENT_NAME_VARIABLE)
                        .addModifiers(PRIVATE, FINAL)
                        .initializer("this.getClass().getAnnotation(Direct.class).target()")
                        .build(),
                FieldSpec.builder(SynchronousDirectAdapterCache.class, ADAPTER_CACHE_FIELD)
                        .addAnnotation(Inject.class)
                        .build()
        );
    }

    @Override
    protected TypeName methodReturnTypeOf(final Action ramlAction) {
        return TypeName.get(JsonEnvelope.class);
    }

    @Override
    protected CodeBlock methodBodyOf(final Resource resource, Action ramlAction, final ActionMimeTypeDefinition definition) {
        return CodeBlock.of("return $L.directAdapterForComponent($L).process(envelope);", ADAPTER_CACHE_FIELD, COMPONENT_NAME_VARIABLE);
    }

    @Override
    protected String handlesAnnotationValueOf(final Action ramlAction, final ActionMimeTypeDefinition definition, final GeneratorConfig generatorConfig) {
        return ActionMapping.valueOf(ramlAction, definition.getNameType()).getName();
    }
}
