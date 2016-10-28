package uk.gov.justice.services.test.utils.core.random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.checkValidityOfText;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.chooseRandomPosition;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.concat;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.isRandomlyTrue;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class GeneratorUtilTest {

    @Test
    public void shoulGenerateValidStringFromAListOfCharacters() {
        char a[] = "abcef".toCharArray();
        String gen = GeneratorUtil.generateStringFromCharacters(new Random(), a, 2, 3);
        for (String c : gen.split("")) {
            assertTrue("abcef".contains(c));
        }
    }

    @Test
    public void shouldChooseRandomPosition() {
        for (int i = 1; i < 10000; i++) {
            int chosen = chooseRandomPosition(i);
            assertTrue(chosen < i);
            assertTrue(chosen >= 0);
        }
    }

    @Test
    public void shouldConcatenateTwoCharacterArrays() {
        char a[] = "abcef".toCharArray();
        char b[] = "abcef".toCharArray();
        char c[] = concat(a, b);
        Assert.assertArrayEquals("abcefabcef".toCharArray(), c);
    }

    @Test
    public void shouldTestThatStringIsComprisedOfValidCharacters() {
        char a[] = "abcef".toCharArray();
        char b[] = "abcef".toCharArray();
        assertTrue(checkValidityOfText(new String(a), b));
        assertFalse(checkValidityOfText(new String(a), "ghij".toCharArray()));
    }
    
    @Test
    public void shouldTestThatStringIsComprisedOfInValidCharacters() {
        char a[] = "abcef".toCharArray();
        char b[] = "bcef".toCharArray();
        assertFalse(checkValidityOfText(new String(a), b));
    }

    @Test
    public void shouldReturnTrueValueAtleastOnceOutOfOneHundredTries() {
        boolean flag = false;
        for (int i = 0; i < 100; i++) {
            flag = isRandomlyTrue(2);
            if (flag) {
                break;
            }
        }
        assertTrue("100 tries should have set the flag to true ", flag);
    }
}
