package uk.gov.justice.services.event.sourcing.subscription;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class SubscriptionNameQualifierTest {

    @Test
    public void shouldReturnFalseForDifferentSubscriptionNameQualifiers() {
        final SubscriptionNameQualifier subscriptionNameQualifier1 = new SubscriptionNameQualifier("ABC");
        final SubscriptionNameQualifier subscriptionNameQualifier2 = new SubscriptionNameQualifier("EFG");
        assertFalse(subscriptionNameQualifier1.equals(subscriptionNameQualifier2));
    }

    @Test
    public void shouldReturnTrueForSameSubscriptionNameQualifiers() {
        final SubscriptionNameQualifier subscriptionNameQualifier1 = new SubscriptionNameQualifier("ABC");
        final SubscriptionNameQualifier subscriptionNameQualifier2 = new SubscriptionNameQualifier("ABC");
        assertTrue(subscriptionNameQualifier1.equals(subscriptionNameQualifier2));
    }
}