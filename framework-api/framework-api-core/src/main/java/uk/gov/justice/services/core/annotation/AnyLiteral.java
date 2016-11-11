package uk.gov.justice.services.core.annotation;

import javax.enterprise.inject.Any;
import javax.enterprise.util.AnnotationLiteral;

public class AnyLiteral extends AnnotationLiteral<Any> {

    private static final long serialVersionUID = -3118797828842400134L;

    public static AnyLiteral create() {
        return new AnyLiteral();
    }
}
