package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.valueOf;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.chooseRandomPosition;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.generateStringFromCharacters;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.isRandomlyTrue;

import java.util.Random;

/**
 * 
 * The local name part of an email address based on the below rfc <br>
 * https://tools.ietf.org/html/rfc3696
 *
 */
public class LocalPartGenerator {

    /**
     * The minimum length of the local part
     */
    private static final int MIN_LENGTH = 1;
    /**
     * The maximum length of the local part
     */
    private static final int MAX_LENGTH = 64;
    /**
     * List of valid characters in the local part of the email
     */
    private static final char[] LOCALPART_STANDARD_CHARACTERS =
                    "0123456789!#$%&'*+-/=?^_'.{|}~abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    .toCharArray();
    /**
     * Quote are required for the below characters <br>
     * at-sign ("@"), backslash, double quote, comma, or square brackets
     */
    private static final int[] LOCALPART_NONSTANDARD_CHARACTERS = new int[] {64, 92, 34, 44, 91, 93};

    /**
     * Random used for character generation
     */
    private final Random random;

    /**
     * Prevent instantiation outside the package
     */
    LocalPartGenerator(final Random random) {
        this.random = random;
    }

    /**
     * Generate the local part
     * 
     * @return the local part
     */
    public String next() {
        return generateLocalPart();
    }

    /**
     * Generate local part
     * 
     * @param text input
     * @return text to generate
     */
    private String generateLocalPart() {
        String generated = generateStringFromCharacters(random, LOCALPART_STANDARD_CHARACTERS,
                        MIN_LENGTH, MAX_LENGTH);
        // toss dice
        if (isRandomlyTrue(6)) {
            final int positionToInsert = chooseRandomPosition(generated.length());
            final char nonstandardCharacter =
                            (char) LOCALPART_NONSTANDARD_CHARACTERS[chooseRandomPosition(
                                            LOCALPART_NONSTANDARD_CHARACTERS.length)];
            // flip coin
            final boolean backSlashQuoteType = isRandomlyTrue(2);
            if (backSlashQuoteType) {
                generated = String.join("", generated.substring(0, positionToInsert), "\\",
                                valueOf(nonstandardCharacter),
                                generated.substring(positionToInsert, generated.length()));
            } else {
                generated = String.join("", "\"", generated.substring(0, positionToInsert),
                                valueOf(nonstandardCharacter),
                                generated.substring(positionToInsert, generated.length()), "\"");
            }
        }
        if (passBasicChecks(generated)) {
            return generated;
        }

        return generateLocalPart();
    }
    

    /**
     * Check the local part of the email
     * 
     * @param textToCheck
     * @return flag indicating check passed
     */
    public static boolean passBasicChecks(final String textToCheck) {
        return !textToCheck.isEmpty() && !(MAX_LENGTH < textToCheck.length())
                        && !textToCheck.startsWith(".") && !textToCheck.endsWith(".")
                        && !textToCheck.matches("[0-9]+");
    }

}
