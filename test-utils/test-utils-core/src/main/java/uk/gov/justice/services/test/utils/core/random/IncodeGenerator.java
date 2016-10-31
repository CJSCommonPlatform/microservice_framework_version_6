package uk.gov.justice.services.test.utils.core.random;

import java.util.Random;

/**
 * 
 * This class is used to generate the second part of the Postcode
 * <br>
 * The following characters are never used in the <b>inward part</b> of the Postcode. <br>
 * CIKMOV
 *
 *
 * @see PostcodeGenerator
 */
public class IncodeGenerator {

    /**
     * The format to be used to generate the inward part of the postcode
     */
    private static final String FORMAT = "NAA";

    /**
     * Letters that are valid
     */
    private static final String VALID_LETTER = "ABDEFGHJLNPQRSTUWXYZ";

    /**
     * Random generator
     */
    private final Random random;

    /**
     * Package specific to prevent instantiation
     */
    IncodeGenerator(final Random random) {
        this.random = random;
    }


    /**
     * Get the incode for the postcode
     * 
     * @return String
     */
    public String next() {

        final String[] selectedFormatTokens = FORMAT.split("");

        final int lengthValidLetter = VALID_LETTER.length();
                        
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < selectedFormatTokens.length; i++) {
            final String selectedFormatCharacter = selectedFormatTokens[i];
            switch (selectedFormatCharacter) {
                case "A":
                    char c = VALID_LETTER.charAt(random.nextInt(lengthValidLetter));
                    sb.append(c);
                    break;
                case "N":
                    sb.append(random.nextInt(10));
                    break;
            }
        }

        return sb.toString();
    }
}
