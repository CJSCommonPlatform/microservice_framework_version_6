package uk.gov.justice.services.example.cakeshop.query.view.response;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class RecipesViewTest {

    private final static UUID ID = UUID.randomUUID();
    private static final String NAME = "cake";

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void equalsAndHashCode() {
        List<RecipeView> recipes = Collections.singletonList(new RecipeView(ID, NAME));

        RecipesView item1 = new RecipesView(recipes);
        RecipesView item2 = new RecipesView(recipes);
        RecipesView item3 = new RecipesView(Collections.emptyList());
        RecipesView item4 = new RecipesView(Collections.singletonList(new RecipeView(UUID.randomUUID(), NAME)));

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .testEquals();
    }

}