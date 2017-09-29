package uk.gov.justice.services.messaging;

import static java.lang.String.format;

/**
 * Utility for consistently retrieving the qualified name of a Schema.
 */
public class SchemaUtil {

    private static final String QUALIFIED_NAME_PATTERN = "%s/%s";

    /**
     * Returns the qualified path for a schema file.
     *
     * @param component - the component the schema belongs to
     * @param schemaName - the schema filename
     * @return the qualified schema file path
     */
    public static String qualifiedSchemaFilePathFrom(final String component, final String schemaName) {
        return format(QUALIFIED_NAME_PATTERN, component.toLowerCase(), schemaName);
    }

}
