package uk.gov.justice.services.example.cakeshop.domain;

public class Ingredient {

    private final String name;
    private final int quantity;

    public Ingredient(final String name, final int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}
