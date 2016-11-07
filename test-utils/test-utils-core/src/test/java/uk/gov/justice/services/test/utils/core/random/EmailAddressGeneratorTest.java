package uk.gov.justice.services.test.utils.core.random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.EMAIL_ADDRESS;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class EmailAddressGeneratorTest {

    @Test
    public void shouldCheckConstructorIsNotAccessible()
                    throws Exception {
        Constructor<EmailAddressGenerator> c = EmailAddressGenerator.class.getDeclaredConstructor();
        assertFalse(c.isAccessible());
    }

    @Test
    public void shouldGenerateTenThousandValidEmails() {
        for (int i = 0; i < 10000; i++) {
            final String email = EMAIL_ADDRESS.next();
            assertTrue(email.contains("@"));
            final String localPart = email.substring(0, email.lastIndexOf("@"));
            final String domain = email.substring(email.lastIndexOf("@")+1, email.length());
            assertTrue(localPart, LocalPartGenerator.passBasicChecks(localPart));
            final String domainPartTokens[] = domain.split(".");
            for(String domainPart: domainPartTokens){
                assertTrue(domainPart, DomainPartGenerator.passBasicChecks(domainPart));
            }
        }
    }
}