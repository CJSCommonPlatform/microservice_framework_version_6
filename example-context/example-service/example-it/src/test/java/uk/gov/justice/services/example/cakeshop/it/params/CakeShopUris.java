package uk.gov.justice.services.example.cakeshop.it.params;

public class CakeShopUris {

    private static final String RANDOM_HTTP_PORT = System.getProperty("random.http.port");
    private static final String HOST = "http://localhost:" + RANDOM_HTTP_PORT ;


    public static final String RECIPES_RESOURCE_URI = HOST + "/example-command-api/command/api/rest/cakeshop/recipes/";
    public static final String ORDERS_RESOURCE_URI = HOST + "/example-command-api/command/api/rest/cakeshop/orders/";
    public static final String RECIPES_RESOURCE_QUERY_URI = HOST + "/example-query-api/query/api/rest/cakeshop/recipes/";
    public static final String ORDERS_RESOURCE_QUERY_URI = HOST + "/example-query-api/query/api/rest/cakeshop/orders/";
    public static final String CAKES_RESOURCE_QUERY_URI = HOST + "/example-query-api/query/api/rest/cakeshop/cakes/";
    public static final String OVEN_RESOURCE_CUSTOM_URI = HOST + "/example-custom-api/custom/api/rest/cakeshop/ovens/";

    public static final String CAKES_RESOURCE_URI_FORMAT = RECIPES_RESOURCE_URI + "%s/cakes/%s";
}
