package uk.gov.justice.services.test.utils.core.random;

import static org.apache.commons.lang3.ArrayUtils.toObject;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

/**
 * This class is used to generate the second part of the Postcode
 * <br>
 * The following characters are never used in the <b>inward part</b> of the Postcode. <br>
 * CIKMOV
 *
 * @see PostcodeGenerator
 */
public class InCodeGenerator extends Generator<String> {

    /**
     * The format to be used to generate the inward part of the postcode
     */
    private static final String FORMAT = "NAA";

    /**
     * Letters that are valid
     */
    private static final Character[] VALID_LETTERS = toObject("ABDEFGHJLNPQRSTUWXYZ".toCharArray());


    /**
     * Package specific to prevent instantiation
     */
    InCodeGenerator() {
    }


    /**
     * Get the incode for the postcode
     *
     * @return String
     */
    public String next() {

        final StringBuilder builder = new StringBuilder();

        for (final char selectedFormatCharacter : FORMAT.toCharArray()) {
            switch (selectedFormatCharacter) {
                case 'A':
                    builder.append(values(VALID_LETTERS).next());
                    break;
                case 'N':
                    builder.append(integer(0, 10).next());
                    break;
            }
        }

        return builder.toString();
    }
}
