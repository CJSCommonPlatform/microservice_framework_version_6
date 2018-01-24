package uk.gov.justice.services.generators.commons.config;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.generators.commons.config.GeneratorPropertiesFactory.generatorProperties;
import static uk.gov.justice.services.generators.commons.config.GeneratorPropertiesHelper.serviceComponentOf;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.emptyPathConfigurationWith;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GeneratorPropertiesHelperTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final GeneratorProperties QUERY_API_PROPERTY = generatorProperties()
            .withServiceComponentOf(QUERY_API);

    private static final uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties QUERY_CONTROLLER_PROPERTY = generatorProperties()
            .withServiceComponentOf(QUERY_CONTROLLER);

    private static final uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties QUERY_VIEW_PROPERTY = generatorProperties()
            .withServiceComponentOf(QUERY_VIEW);

    private static final uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties COMMAND_API_PROPERTY = generatorProperties()
            .withServiceComponentOf(COMMAND_API);

    private static final uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties COMMAND_CONTROLLER_PROPERTY = generatorProperties()
            .withServiceComponentOf(COMMAND_CONTROLLER);

    private static final uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties COMMAND_HANDLER_PROPERTY = generatorProperties()
            .withServiceComponentOf(COMMAND_HANDLER);

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(GeneratorPropertiesHelper.class);
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

        serviceComponentOf(emptyPathConfigurationWith(new CommonGeneratorProperties()));
    }
}