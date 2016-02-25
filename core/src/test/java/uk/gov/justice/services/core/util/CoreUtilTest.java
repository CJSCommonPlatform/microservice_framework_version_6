package uk.gov.justice.services.core.util;

import org.junit.Test;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class CoreUtilTest {

    @Test
    public void shouldBeAWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(CoreUtil.class);
    }

    @Test
    public void shouldReturnContextName() {
        assertThat(CoreUtil.extractContextNameFromActionOrEventName("test-context.commands.test-command"), equalTo("test-context"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidActionName() {
        CoreUtil.extractContextNameFromActionOrEventName("test-context-commands-test-command");
    }

}