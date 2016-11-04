package uk.gov.justice.services.test.utils.core.random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.junit.Test;

public class OutcodeGeneratorTest {

    @Test
    public void shouldCheckConstructorIsNotAccessible()
                    throws Exception {
        final Constructor<IncodeGenerator> c = IncodeGenerator.class.getDeclaredConstructor(Random.class);
        assertFalse(c.isAccessible());
    }

    @Test
    public void shouldGenerateTenThousandOutcodeParts() {
        final OutcodeGenerator og = new OutcodeGenerator(new Random());
        for (int i = 0; i < 10000; i++) {
            final String outcode = og.next();
            assertTrue(outcode, outcode.length() <= 4);
            assertTrue(outcode, outcode.length() >= 2);
            final String firstLetter = String.valueOf(outcode.charAt(0));
            assertTrue("ABCDEFGHIJKLMNOPRSTUWYZ".contains(firstLetter));
        }
    }
}
