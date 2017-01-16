package uk.gov.justice.services.clients.messaging.generator;

import static com.squareup.javapoet.TypeName.VOID;
import static uk.gov.justice.services.generators.commons.helper.Names.nameFrom;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.generators.commons.client.AbstractClientGenerator;
import uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;

public class MessagingClientGenerator extends AbstractClientGenerator {

    private static final String SENDER = "sender";

    @Override
    protected String classNameOf(final Raml raml) {
        MessagingResourceUri uri = new MessagingResourceUri(raml.getResources().values().iterator().next().getUri());
        return String.format("Remote%s", uri.toClassName());
    }

    @Override
    protected Iterable<FieldSpec> fieldsOf(final Raml raml) {
        return ImmutableList.of(FieldSpec.builder(JmsEnvelopeSender.class, SENDER)
                .addAnnotation(Inject.class)
                .build());
    }

    @Override
    protected TypeName methodReturnTypeOf(final Action ramlAction) {
        return VOID;
    }

    @Override
    protected String handlesAnnotationValueOf(final Action ramlAction, final ActionMimeTypeDefinition definition, final GeneratorConfig generatorConfig) {
        return nameFrom(definition.getNameType());
    }

    @Override
    protected CodeBlock methodBodyOf(final Resource resource, final Action ramlAction, final ActionMimeTypeDefinition definition) {
        final MessagingResourceUri resourceUri = new MessagingResourceUri(resource.getUri());
        return CodeBlock.builder()
                .addStatement("$L.send($L, $S)", SENDER, ENVELOPE, resourceUri.destinationName())
                .build();
    }


}