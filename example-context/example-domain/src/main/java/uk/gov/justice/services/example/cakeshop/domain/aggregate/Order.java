package uk.gov.justice.services.example.cakeshop.domain.aggregate;

import static uk.gov.justice.domain.aggregate.condition.Precondition.assertPrecondition;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.example.cakeshop.domain.Ingredient;
import uk.gov.justice.services.example.cakeshop.domain.event.CakeOrdered;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Order aggregate.
 */
public class Order implements Aggregate {

    private UUID orderId;

    public Stream<Object> addOrder(final UUID orderId, final UUID recipeId, final ZonedDateTime deliveryDate) {
        assertPrecondition(this.orderId == null).orElseThrow("Order already added");

        return apply(Stream.of(new CakeOrdered(orderId, recipeId, deliveryDate)));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(CakeOrdered.class).apply(cakeOrdered -> orderId = cakeOrdered.getOrderId()));
    }
}
