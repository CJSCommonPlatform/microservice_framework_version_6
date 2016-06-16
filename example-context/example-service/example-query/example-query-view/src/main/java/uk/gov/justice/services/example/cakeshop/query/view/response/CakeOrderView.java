package uk.gov.justice.services.example.cakeshop.query.view.response;

import java.time.ZonedDateTime;
import java.util.UUID;

public class CakeOrderView {
    private UUID orderId;

    private UUID recipeId;

    private ZonedDateTime deliveryDate;


    public CakeOrderView(final UUID orderId, final UUID recipeId, final ZonedDateTime deliveryDate) {
        this.orderId = orderId;
        this.recipeId = recipeId;
        this.deliveryDate = deliveryDate;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public ZonedDateTime getDeliveryDate() {
        return deliveryDate;
    }
}
