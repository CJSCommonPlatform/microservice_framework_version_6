package uk.gov.justice.services.example.cakeshop.persistence.entity;

import java.util.UUID;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class IngredientTest {

    private final static UUID ID = UUID.randomUUID();
    private final static String NAME = "Flour";

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void equalsAndHashCode() {
        Ingredient item1 = new Ingredient(ID, NAME);
        Ingredient item2 = new Ingredient(ID, NAME);
        Ingredient item3 = new Ingredient(UUID.randomUUID(), NAME);
        Ingredient item4 = new Ingredient(ID, null);

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .testEquals();
    }

}