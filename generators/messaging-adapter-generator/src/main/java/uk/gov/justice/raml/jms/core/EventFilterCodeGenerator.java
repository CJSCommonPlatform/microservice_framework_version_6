package uk.gov.justice.raml.jms.core;


import static com.squareup.javapoet.AnnotationSpec.builder;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.raml.jms.core.MediaTypesUtil.mediaTypesFrom;
import static uk.gov.justice.services.generators.commons.helper.Names.DEFAULT_ANNOTATION_PARAMETER;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;
import static uk.gov.justice.services.generators.commons.helper.Names.namesListStringFrom;

import uk.gov.justice.services.generators.commons.helper.MessagingBaseUri;
import uk.gov.justice.services.event.buffer.api.AbstractEventFilter;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Resource;

class EventFilterCodeGenerator {

    TypeSpec generatedCodeFor(final Resource resource, final MessagingBaseUri baseUri) {
        return classBuilder(classNameOf(baseUri))
                .addModifiers(PUBLIC)
                .superclass(AbstractEventFilter.class)
                .addAnnotation(ApplicationScoped.class)
                .addAnnotation(Alternative.class)
                .addAnnotation(builder(Priority.class).addMember(DEFAULT_ANNOTATION_PARAMETER, "2").build())
                .addMethod(constructorBuilder()
                        .addModifiers(PUBLIC)
                        .addCode("super(\"$L\");", namesListStringFrom(mediaTypesFrom(resource.getActions()), "\",\"")).build())
                .build();
    }

    /**
     * Create class name basing on the base URI string
     *
     * @param baseUri URI String to convert
     * @return camel case class name
     */
    private String classNameOf(final MessagingBaseUri baseUri) {
        return format("%sEventFilter", buildJavaFriendlyName(baseUri.service()));
    }


}
