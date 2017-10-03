package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class FixedPositionValueTest {

    @Test
    public void shouldReturnHeadPositionValue() throws Exception {
        assertThat(FixedPositionValue.HEAD, is("HEAD"));
    }

    @Test
    public void shouldReturnFirstPositionValue() throws Exception {
        assertThat(FixedPositionValue.FIRST, is("1"));
    }

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(FixedPositionValue.class);
    }

}