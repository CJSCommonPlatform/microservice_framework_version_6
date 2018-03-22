package uk.gov.justice.subscription.jms.interceptor;

import static javax.lang.model.element.Modifier.PRIVATE;

import javax.inject.Inject;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;

public class EventFilterFieldCodeGenerator {

    private static final String EVENT_FILTER_FIELD_NAME = "eventFilter";

    public FieldSpec createEventFilterField(final ClassName eventFilterClassName) {
        return FieldSpec.builder(eventFilterClassName, EVENT_FILTER_FIELD_NAME, PRIVATE)
                .addAnnotation(Inject.class)
                .build();
    }
}
