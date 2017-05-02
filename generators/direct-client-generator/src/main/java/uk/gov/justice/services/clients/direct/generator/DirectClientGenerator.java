package uk.gov.justice.services.clients.direct.generator;

import static java.lang.String.format;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
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

import java.util.Collections;

import javax.inject.Inject;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;


public class DirectClientGenerator extends AbstractClientGenerator {

    private static final String ADAPTER_FIELD = "adapter";

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
    protected String classNameOf(final Raml raml) {
        return buildJavaFriendlyName(format("Direct%sClient", new RestResourceBaseUri(raml.getBaseUri()).pathWithoutWebContext()));
    }

    @Override
    protected Class<?> classAnnotation() {
        return Direct.class;
    }

    @Override
    protected Iterable<FieldSpec> fieldsOf(final Raml raml) {
        return Collections.singletonList(FieldSpec.builder(SynchronousDirectAdapter.class, ADAPTER_FIELD)
                .addAnnotation(Inject.class)
                .build());
    }

    @Override
    protected TypeName methodReturnTypeOf(final Action ramlAction) {
        return TypeName.get(JsonEnvelope.class);
    }

    @Override
    protected CodeBlock methodBodyOf(final Resource resource, Action ramlAction, final ActionMimeTypeDefinition definition) {
        return CodeBlock.of("return $L.process(envelope);", ADAPTER_FIELD);
    }

    @Override
    protected String handlesAnnotationValueOf(final Action ramlAction, final ActionMimeTypeDefinition definition, final GeneratorConfig generatorConfig) {
        return ActionMapping.valueOf(ramlAction, definition.getNameType()).getName();
    }
}
