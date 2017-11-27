package uk.gov.justice.services.example.pojo;

public class CakeOrder {

    private String recipeId;
    private String deliveryDate;
    private String orderId;

    public CakeOrder(final String recipeId, final String deliveryDate, final String orderId) {
        this.recipeId = recipeId;
        this.deliveryDate = deliveryDate;
        this.orderId = orderId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public String getOrderId() {
        return orderId;
    }
}
