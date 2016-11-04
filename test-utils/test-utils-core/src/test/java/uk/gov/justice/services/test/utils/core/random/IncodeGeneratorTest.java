package uk.gov.justice.services.test.utils.core.random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.junit.Test;

public class IncodeGeneratorTest {

    @Test
    public void shouldCheckConstructorIsNotAccessible()
                    throws NoSuchMethodException, SecurityException {
        final Constructor<IncodeGenerator> c = IncodeGenerator.class.getDeclaredConstructor(Random.class);
        assertFalse(c.isAccessible());
    }

    @Test
    public void shouldGenerateTenThousandIncodeParts() {
        final IncodeGenerator lpg = new IncodeGenerator(new Random());
        for (int i = 0; i < 10000; i++) {
            final String incode = lpg.next();
            assertTrue( incode.matches("[A-Z0-9]*"));
            assertEquals(3, incode.length());
        }
    }
    
}