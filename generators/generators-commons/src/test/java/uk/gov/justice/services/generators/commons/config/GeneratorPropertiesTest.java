package uk.gov.justice.services.generators.commons.config;

import static java.util.Collections.emptyMap;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.generators.commons.config.GeneratorProperties.serviceComponentOf;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.emptyPathConfigurationWith;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GeneratorPropertiesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final Map<String, String> QUERY_API_PROPERTY = generatorProperties()
            .withServiceComponentOf(QUERY_API)
            .build();

    private static final Map<String, String> QUERY_CONTROLLER_PROPERTY = generatorProperties()
            .withServiceComponentOf(QUERY_CONTROLLER)
            .build();

    private static final Map<String, String> QUERY_VIEW_PROPERTY = generatorProperties()
            .withServiceComponentOf(QUERY_VIEW)
            .build();

    private static final Map<String, String> COMMAND_API_PROPERTY = generatorProperties()
            .withServiceComponentOf(COMMAND_API)
            .build();

    private static final Map<String, String> COMMAND_CONTROLLER_PROPERTY = generatorProperties()
            .withServiceComponentOf(COMMAND_CONTROLLER)
            .build();

    private static final Map<String, String> COMMAND_HANDLER_PROPERTY = generatorProperties()
            .withServiceComponentOf(COMMAND_HANDLER)
            .build();

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(GeneratorProperties.class);
    }

    @Test
    public void shouldReturnQueryApi() throws Exception {
        assertThat(serviceComponentOf(emptyPathConfigurationWith(QUERY_API_PROPERTY)), is(QUERY_API));
    }

    @Test
    public void shouldReturnQueryController() throws Exception {
        assertThat(serviceComponentOf(emptyPathConfigurationWith(QUERY_CONTROLLER_PROPERTY)), is(QUERY_CONTROLLER));
    }

    @Test
    public void shouldReturnQueryView() throws Exception {
        assertThat(serviceComponentOf(emptyPathConfigurationWith(QUERY_VIEW_PROPERTY)), is(QUERY_VIEW));
    }

    @Test
    public void shouldReturnCommandApi() throws Exception {
        assertThat(serviceComponentOf(emptyPathConfigurationWith(COMMAND_API_PROPERTY)), is(COMMAND_API));
    }

    @Test
    public void shouldReturnCommandController() throws Exception {
        assertThat(serviceComponentOf(emptyPathConfigurationWith(COMMAND_CONTROLLER_PROPERTY)), is(COMMAND_CONTROLLER));
    }

    @Test
    public void shouldReturnCommandHandler() throws Exception {
        assertThat(serviceComponentOf(emptyPathConfigurationWith(COMMAND_HANDLER_PROPERTY)), is(COMMAND_HANDLER));
    }

    @Test
    public void shouldThrowExceptionIfGeneratorPropertiesAreNullForServiceComponent() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("serviceComponent generator property not set in the plugin config");

        serviceComponentOf(emptyPathConfigurationWith(null));
    }

    @Test
    public void shouldThrowExceptionIfServiceComponentPropertyNotSet() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("serviceComponent generator property not set in the plugin config");

        serviceComponentOf(emptyPathConfigurationWith(emptyMap()));
    }
}