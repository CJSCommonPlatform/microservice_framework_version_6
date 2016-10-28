package uk.gov.justice.services.test.utils.core.random;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 
 * Utility class used by the email generator
 * 
 * @See EmailAddressGenerator
 *
 */
public class GeneratorUtil {

    /**
     * Private constructor to prevent instantiation
     */
    private GeneratorUtil() {}

    
    public static final List<String> getTopLevelDomains(){
        try {
            final Properties props = new Properties();
            props.load(GeneratorUtil.class.getClassLoader().getResourceAsStream("tlds.txt"));
            return props.keySet().stream().map(o -> (String) o).collect(Collectors.toList());
        } catch (IOException | NullPointerException e) {
            // fall back to some tlds
            return Arrays.asList(new String[]{"gov.uk", "co.uk", "org", "net", "com"});
        }
    }
    

    /**
     * Generate a random string from an array of characters
     * 
     * @param validChars to chose from
     * @param min length of the string
     * @param max length of the string
     * @return
     */
    public static String generateStringFromCharacters(final Random random, final char[] validChars, final int min,
                    final int max) {
        final int size = ThreadLocalRandom.current().nextInt(min, max + 1);
        final char[] localPart = new char[size];
        for (int i = 0; i < size; i++) {
            localPart[i] = validChars[random.nextInt(validChars.length - 1)];
        }
        return new String(localPart);
    }

    /**
     * Choose a random position given a length
     * 
     * @param number
     * @return
     */
    public static int chooseRandomPosition(final int length) {
        return new Random().nextInt(length);
    }

    /**
     * Concatenate multiple character arrays
     * 
     * @param tokens to concatenate
     * @return concatenated character array
     */
    public static char[] concat(final char[]... tokens) {
        int size = 0;
        for (char[] token : tokens) {
            size += token.length;
        }
        char[] concatenated = new char[size];
        int index = 0;
        for (char[] token : tokens) {
            for (char c : token) {
                concatenated[index++] = c;
            }
        }
        return concatenated;
    }

    /**
     * Check that the text contains only the valid characters mentioned
     * 
     * @param text to check
     * @param valid characters
     * @return flag indicating that the check passed
     */
    public static boolean checkValidityOfText(final String text, final char[] valid) {
        final String validString = new String(valid);
        final String[] local = text.split("");
        for (final String c : local) {
            if(!validString.contains(c)){
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * Randomly return true within a bound<br>
     * If the bound is n, returns approximately 1 in n as true <br>
     * If the bound is 6, returns approximately 1 in 6 as true <br>
     * If the bound is 2, returns approximately 1 in 2 as true
     * <br>
     * 
     * @param bound
     * @return flag is true randomly within a bound
     */
    public static boolean isRandomlyTrue(final int bound) {
        return new Random().nextInt(bound) == 0;
    }
    
   
}
