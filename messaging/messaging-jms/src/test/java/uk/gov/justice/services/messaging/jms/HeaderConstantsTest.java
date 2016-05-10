package uk.gov.justice.services.messaging.jms;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;

import org.junit.Test;

/**
 * Unit tests for the {@link HeaderConstants} test.
 */
public final class HeaderConstantsTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(HeaderConstants.class);
    }

}
