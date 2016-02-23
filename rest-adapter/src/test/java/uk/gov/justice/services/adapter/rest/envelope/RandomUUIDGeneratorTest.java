package uk.gov.justice.services.adapter.rest.envelope;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link RandomUUIDGenerator} class.
 */
public class RandomUUIDGeneratorTest {

    private RandomUUIDGenerator generator;

    @Before
    public void setup() {
        generator = new RandomUUIDGenerator();
    }

    @Test
    public void shouldReturnUUID() {
        assertThat(generator.generate(), not(nullValue()));
    }

    @Test
    public void shouldReturnDifferentUUIDs() {
        UUID uuidA = generator.generate();
        UUID uuidB = generator.generate();
        assertThat(uuidA, not(equalTo(uuidB)));
    }
}
