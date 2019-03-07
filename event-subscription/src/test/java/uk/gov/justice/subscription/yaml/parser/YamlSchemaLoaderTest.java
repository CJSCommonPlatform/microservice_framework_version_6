package uk.gov.justice.subscription.yaml.parser;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import org.everit.json.schema.Schema;
import org.junit.Test;

public class YamlSchemaLoaderTest {

    private static final String EVENT_SOURCES_SCHEMA_PATH = "/json/schema/event-source-schema.json";

    @Test
    public void shouldCreateSchema() throws Exception {
        final YamlSchemaLoader yamlSchemaLoader = new YamlSchemaLoader();
        final Schema schema = yamlSchemaLoader.loadSchema(EVENT_SOURCES_SCHEMA_PATH);

        assertThat(schema, is(notNullValue()));
    }
}