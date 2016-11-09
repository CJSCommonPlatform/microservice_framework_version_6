package uk.gov.justice.services.test.utils.core.random;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Utility class used by the email generator
 *
 * @See EmailAddressGenerator
 */
public class GeneratorUtil {

    /**
     * Private constructor to prevent instantiation
     */
    private GeneratorUtil() {
    }


    public static List<String> getTopLevelDomains() {
        try {
            final Properties props = new Properties();
            props.load(GeneratorUtil.class.getClassLoader().getResourceAsStream("top-level-domains.txt"));
            return props.keySet().stream().map(o -> (String) o).collect(toList());
        } catch (final IOException | NullPointerException e) {
            // fall back to some tlds
            return asList("gov.uk", "co.uk", "org", "net", "com");
        }
    }


    /**
     * Generate a random string from an array of characters
     *
     * @param validChars to chose from
     * @param min        length of the string
     * @param max        length of the string
     */
    public static String generateStringFromCharacters(final char[] validChars, final int min, final int max) {
        final int size = integer(min, max + 1).next();
        final char[] localPart = new char[size];
        for (int i = 0; i < size; i++) {
            localPart[i] = values(toObject(validChars)).next();
        }
        return new String(localPart);
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
        final char[] concatenated = new char[size];
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
     * @param text  to check
     * @param valid characters
     * @return flag indicating that the check passed
     */
    public static boolean checkValidityOfText(final String text, final char[] valid) {
        final String validString = new String(valid);
        final String[] local = text.split("");
        for (final String c : local) {
            if (!validString.contains(c)) {
                return false;
            }
        }
        return true;
    }

}
