package uk.gov.justice.services.example.cakeshop.custom.api.response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

public class OvensStatusTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateOvensStatusWithGivenValues() throws Exception {
        final List ovens = mock(List.class);
        assertThat(new OvensStatus(ovens).getOvens(), is(ovens));
    }
}