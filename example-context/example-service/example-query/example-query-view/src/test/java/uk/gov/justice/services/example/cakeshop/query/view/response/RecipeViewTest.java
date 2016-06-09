package uk.gov.justice.services.example.cakeshop.query.view.response;

import java.util.UUID;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class RecipeViewTest {

    private final static UUID ID = UUID.randomUUID();
    private static final String NAME = "cake";

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void equalsAndHashCode() {
        RecipeView item1 = new RecipeView(ID, NAME, false);
        RecipeView item2 = new RecipeView(ID, "cake", false);
        RecipeView item3 = new RecipeView(UUID.randomUUID(), "cake", false);
        RecipeView item4 = new RecipeView(ID, "Different", false);

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .testEquals();
    }

}