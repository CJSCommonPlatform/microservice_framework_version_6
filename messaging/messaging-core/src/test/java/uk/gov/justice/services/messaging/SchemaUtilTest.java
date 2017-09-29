package uk.gov.justice.services.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SchemaUtilTest {

    @Test
    public void shouldReturnQualifiedSchema() {
        final String component = "component_api";
        final String schemaFileName = "example.add-recipe.json";

        final String qualifiedSchemaName = SchemaUtil.qualifiedSchemaFilePathFrom(component, schemaFileName);

        assertThat(qualifiedSchemaName, is("component_api/example.add-recipe.json"));
    }

    @Test
    public void shouldReturnQualifiedSchemaIgnoringComponentCase() {
        final String component = "COMPONENT_API";
        final String schemaFileName = "example.add-recipe.json";

        final String qualifiedSchemaName = SchemaUtil.qualifiedSchemaFilePathFrom(component, schemaFileName);

        assertThat(qualifiedSchemaName, is("component_api/example.add-recipe.json"));
    }
}