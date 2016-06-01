package uk.gov.justice.services.adapter.rest.cors;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;

import org.junit.Test;

/**
 * Unit tests for the {@link CorsHeaders class}.
 */
public class CorsHeadersTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(CorsHeaders.class);
    }
}
