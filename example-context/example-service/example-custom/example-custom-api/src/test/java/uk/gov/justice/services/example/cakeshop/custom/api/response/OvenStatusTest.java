package uk.gov.justice.services.example.cakeshop.custom.api.response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Test;

public class OvenStatusTest {

    @Test
    public void shouldCreateOvenStatusWithGivenValues() throws Exception {
        final String name = "name";
        final UUID id = UUID.randomUUID();
        final int temperature = 200;
        final boolean active = true;
        final OvenStatus ovenStatus = new OvenStatus(id, name, temperature, active);

        assertThat(ovenStatus.getName(), is(name));
        assertThat(ovenStatus.getId(), is(id));
        assertThat(ovenStatus.getTemperature(), is(200));
        assertThat(ovenStatus.isActive(), is(active));
    }
}