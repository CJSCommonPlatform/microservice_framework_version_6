package uk.gov.justice.services.event.sourcing.subscription;


import uk.gov.justice.services.subscription.annotation.SubscriptionName;

import java.util.Objects;

import javax.enterprise.util.AnnotationLiteral;

public class SubscriptionNameQualifier extends AnnotationLiteral<SubscriptionName> implements SubscriptionName {

    private final String value;

    public SubscriptionNameQualifier(final String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final SubscriptionNameQualifier that = (SubscriptionNameQualifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}