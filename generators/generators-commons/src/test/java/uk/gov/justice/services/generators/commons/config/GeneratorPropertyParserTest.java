package uk.gov.justice.services.generators.commons.config;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.emptyPathConfigurationWith;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GeneratorPropertyParserTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final Map<String, String> QUERY_API_PROPERTY = generatorProperties()
            .withServiceComponentOf(QUERY_API)
            .build();

    private static final Map<String, String> QUERY_API_PROPERTY_WITH_MDB_POOL = generatorProperties()
            .withServiceComponentOf(QUERY_API)
            .withCustomMDBPool()
            .build();

    @Test
    public void shouldReturnQueryApi() throws Exception {
        final GeneratorPropertyParser generatorPropertyParser = new GeneratorPropertyParser(emptyPathConfigurationWith(QUERY_API_PROPERTY));
        assertThat(generatorPropertyParser.serviceComponent(), is(QUERY_API));
    }

    @Test
    public void shouldThrowExceptionIfGeneratorPropertiesAreNullForServiceComponent() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("serviceComponent generator property not set in the plugin config");

        new GeneratorPropertyParser(emptyPathConfigurationWith(null)).serviceComponent();
    }

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotSet() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("serviceComponent generator property not set in the plugin config");

        new GeneratorPropertyParser(emptyPathConfigurationWith(null)).serviceComponent();
    }

    @Test
    public void shouldReturnCustomMdbToolAsTrue() throws Exception {
        final GeneratorPropertyParser generatorPropertyParser = new GeneratorPropertyParser(emptyPathConfigurationWith(QUERY_API_PROPERTY_WITH_MDB_POOL));
        assertThat(generatorPropertyParser.shouldAddCustomPoolConfiguration(), is(true));
    }

    @Test
    public void shouldReturnCustomMdbToolAsFalse() throws Exception {
        final GeneratorPropertyParser generatorPropertyParser = new GeneratorPropertyParser(emptyPathConfigurationWith(QUERY_API_PROPERTY));
        assertThat(generatorPropertyParser.shouldAddCustomPoolConfiguration(), is(false));
    }

    @Test
    public void shouldThrowExceptionIfGeneratorPropertiesAreNullForCustomMdbTool() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("customMDBPool generator property not set in the plugin config");

        new GeneratorPropertyParser(emptyPathConfigurationWith(null)).shouldAddCustomPoolConfiguration();
    }
}
