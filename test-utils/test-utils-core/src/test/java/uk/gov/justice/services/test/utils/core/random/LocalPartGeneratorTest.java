package uk.gov.justice.services.test.utils.core.random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.checkValidityOfText;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.concat;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalPartGeneratorTest {

    static char[] valid = null;

    @BeforeClass
    public static void beforeClass() {
        char[] nonStandard = new char[6];
        int index = 0;
        for (int c : new int[] {64, 92, 34, 44, 91, 93}) {
            nonStandard[index++] = (char) c;
        }
        char[] escape = new char[2];
        escape[0] = (char) 92;
        escape[1] = (char) 34;

        valid = concat("0123456789!#$%&'*+-/=?^_'.{|}~abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        .toCharArray(), nonStandard, escape);
    }

    @Test
    public void shouldCheckConstructorIsNotAccessible()
                    throws NoSuchMethodException, SecurityException {
        final Constructor<LocalPartGenerator> c =
                        LocalPartGenerator.class.getDeclaredConstructor(Random.class);
        assertFalse(c.isAccessible());
    }

    @Test
    public void shouldGenerateTenThousandLocalParts() {
        final LocalPartGenerator lpg = new LocalPartGenerator(new Random());
        for (int i = 0; i < 10000; i++) {
            final String localPart = lpg.next();
            assertTrue(checkValidityOfText(localPart, valid));
        }
    }


    @Test
    public void shouldCheckLocalPartDoesNotStartWithDot() {
        assertFalse(LocalPartGenerator.passBasicChecks(".halloween"));
    }

    @Test
    public void shouldCheckLocalPartDoesNotEndWithDot() {
        assertFalse(LocalPartGenerator.passBasicChecks("halloween."));
    }

    @Test
    public void shouldCheckLocalPartIsNotEmpty() {
        assertFalse(LocalPartGenerator.passBasicChecks(""));
    }

    @Test
    public void shouldCheckLocalPartIsValid() {
        assertTrue(LocalPartGenerator.passBasicChecks("halloween"));
    }

    @Test
    public void shouldCheckLocalPartLongerThanMaximumLengthIsInvalid() {
        assertFalse(LocalPartGenerator.passBasicChecks(RandomStringUtils.randomAlphanumeric(65)));
    }

}


