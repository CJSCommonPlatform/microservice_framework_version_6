package uk.gov.justice.services.example.cakeshop.it.params;

import static java.lang.String.format;

public class CakeShopMediaTypes {

    public static final String CONTEXT_NAME = "example";

    private static final String PREFIX = format("application/vnd.%s.", CONTEXT_NAME);

    public static final String ADD_RECIPE_MEDIA_TYPE = PREFIX + "add-recipe+json";
    public static final String RENAME_RECIPE_MEDIA_TYPE = PREFIX + "rename-recipe+json";
    public static final String REMOVE_RECIPE_MEDIA_TYPE = PREFIX + "remove-recipe+json";
    public static final String MAKE_CAKE_MEDIA_TYPE = PREFIX + "make-cake+json";
    public static final String MAKE_CAKE_STATUS_MEDIA_TYPE = PREFIX + "make-cake-status+json";
    public static final String ORDER_CAKE_MEDIA_TYPE = PREFIX + "order-cake+json";

    public static final String QUERY_RECIPE_MEDIA_TYPE = PREFIX + "recipe+json";
    public static final String QUERY_RECIPES_MEDIA_TYPE = PREFIX + "recipes+json";
    public static final String QUERY_CAKES_MEDIA_TYPE = PREFIX + "cakes+json";
    public static final String QUERY_ORDER_MEDIA_TYPE = PREFIX + "order+json";
    public static final String POST_RECIPES_QUERY_MEDIA_TYPE = PREFIX + "query-recipes+json";
}
