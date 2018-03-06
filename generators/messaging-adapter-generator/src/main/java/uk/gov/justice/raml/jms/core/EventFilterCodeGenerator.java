package uk.gov.justice.raml.jms.core;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.raml.jms.core.MediaTypesUtil.mediaTypesFrom;
import static uk.gov.justice.services.generators.commons.helper.Names.namesListStringFrom;

import uk.gov.justice.services.event.buffer.api.AbstractEventFilter;

import javax.enterprise.context.ApplicationScoped;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Resource;

class EventFilterCodeGenerator {

    TypeSpec generate(final Resource resource, final ClassNameFactory classNameFactory) {

        return classBuilder(classNameFactory.classNameWith("EventFilter"))
                .addModifiers(PUBLIC)
                .superclass(AbstractEventFilter.class)
                .addAnnotation(ApplicationScoped.class)
                .addMethod(constructorBuilder()
                        .addModifiers(PUBLIC)
                        .addCode("super(\"$L\");", namesListStringFrom(mediaTypesFrom(resource.getActions()), "\",\"")).build())
                .build();
    }
}
