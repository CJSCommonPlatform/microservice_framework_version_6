package uk.gov.justice.services.test.utils.core.random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class PostcodeGeneratorTest {

    @Test
    public void shouldCheckConstructorIsNotAccessible()
                    throws Exception {
        final Constructor<PostcodeGenerator> c =
                        PostcodeGenerator.class.getDeclaredConstructor();
        assertFalse(c.isAccessible());
    }

    @Test
    public void shouldGenerateTenThousandPostcodes() {        
        final PostcodeGenerator og = new PostcodeGenerator();
        for (int i = 0; i < 10000; i++) {
            final String postcode = og.next();
            assertTrue(postcode.contains(" "));
            assertTrue(postcode.length() > 3);
            assertTrue(postcode.length() <= 8);
            final String tokens[] = postcode.split(" ");
            assertTrue(tokens[1].substring(1).matches("[ABDEFGHJLNPQRSTUWXYZ]*"));
            assertTrue(tokens[1].substring(0,1).matches("[0-9]*"));
            assertTrue(tokens[0].matches("[A-Z0-9]*"));
        }
    }
    
    
}
