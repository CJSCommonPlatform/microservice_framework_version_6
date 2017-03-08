package uk.gov.justice.services.example.cakeshop.query.controller;


import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.Test;

public class RecipesQueryControllerTest {

    @Test
    public void shouldBeQueryControllerThatHasRequesterPassThroughMethods() throws Exception {
        assertThat(RecipesQueryController.class, isHandlerClass(QUERY_CONTROLLER)
                .with(allOf(
                        method("listRecipes")
                                .thatHandles("example.search-recipes")
                                .withRequesterPassThrough(),
                        method("recipe")
                                .thatHandles("example.get-recipe")
                                .withRequesterPassThrough(),
                        method("queryRecipes")
                                .thatHandles("example.query-recipes")
                                .withRequesterPassThrough(),
                        method("getRecipePhotograph")
                                .thatHandles("example.get-recipe-photograph")
                                .withRequesterPassThrough()
                )));
    }
}
