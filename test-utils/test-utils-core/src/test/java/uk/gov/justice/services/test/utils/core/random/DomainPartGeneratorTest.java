package uk.gov.justice.services.test.utils.core.random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.checkValidityOfText;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.concat;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomainPartGeneratorTest {

    private static char[] valid = null;

    @BeforeClass
    public static void beforeClass() {
        char[] escape = new char[3];
        escape[0] = (char) 46; // full stop
        escape[1] = '(';
        escape[2] = ')';
        valid = concat("-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                        .toCharArray(), escape);
    }

    @Test
    public void shouldCheckConstructorIsNotAccessible()
                    throws NoSuchMethodException, SecurityException {
        final Constructor<DomainPartGenerator> c =
                        DomainPartGenerator.class.getDeclaredConstructor(Random.class);
        assertFalse(c.isAccessible());
    }

    @Test
    public void shouldGenerateTenThousandDomainParts() {
        final DomainPartGenerator dpg = new DomainPartGenerator(new Random());
        dpg.setTopLevelDomains(
                        Arrays.asList(new String[] {"gov.uk", "co.uk", "org", "net", "com"}));
        for (int i = 0; i < 10000; i++) {
            final String domainPart = dpg.next();
            assertTrue(domainPart, checkValidityOfText(domainPart, valid));
        }
    }
    
    @Test
    public void shouldCheckDomainPartDoesNotStartWithHyphen() {
        assertFalse(DomainPartGenerator.passBasicChecks("-test.co.uk"));
    }
    
    @Test
    public void shouldCheckDomainPartDoesNotEndWithHyphen() {
        assertFalse(DomainPartGenerator.passBasicChecks("test.co.uk-"));
    }
    
    @Test
    public void shouldCheckDomainPartIsNotAllNumeric() {
        assertFalse(DomainPartGenerator.passBasicChecks("1234567"));
    }
    
    @Test
    public void shouldCheckDomainPartIsNotEmpty() {
        assertFalse(DomainPartGenerator.passBasicChecks(""));
    }
    
    @Test
    public void shouldCheckDomainPartIsValid() {
        assertTrue(DomainPartGenerator.passBasicChecks("hmcts.net"));
    }
    
    @Test
    public void shouldCheckDomainPartLongerThanMaximumIsInvalid() {
        assertFalse(DomainPartGenerator.passBasicChecks(RandomStringUtils.randomAlphanumeric(65)));
    }
    
}
