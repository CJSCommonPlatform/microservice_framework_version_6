package uk.gov.justice.services.test.utils.core.random;

import static org.apache.commons.lang3.ArrayUtils.toObject;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

/**
 * This class is used to generate the first part of the Postcode
 *
 * <ul>
 * <li>The letters Q, V and X are not used in the first position</li>
 * <li>The letters I,J and Z are not used in the second position.</li>
 * <li>The only letters to appear in the third position are A, B, C, D, E, F, G, H, J, K, S, T, U
 * and W.</li>
 * </ul>
 */
public class OutCodeGenerator extends Generator<String> {

    /**
     * Letters that are valid in the first position
     */
    private static final Character[] VALID_FIRST_LETTERS = toObject("ABCDEFGHIJKLMNOPRSTUWYZ".toCharArray());

    /**
     * Letters that are valid in the second position
     */
    private static final Character[] VALID_SECOND_LETTERS = toObject("ABCDEFGHKLMNOPQRSTUVWXY".toCharArray());

    /**
     * Letters that are valid in the third position
     */
    private static final Character[] VALID_THIRD_LETTERS = toObject("ABCDEFGHJKSTUW".toCharArray());

    /**
     * List of formats
     */
    private static final String[] FORMATS = new String[]{"AN", "ANN", "AAN", "AANN", "ANA", "AANA"};

    /**
     * Package specific to prevent instantiation
     */
    OutCodeGenerator() {

    }

    /**
     * Get the outcode for the postcode
     *
     * @return String
     */
    public String next() {

        final String selectedFormat = values(FORMATS).next();
        final char[] selectedFormatChars = selectedFormat.toCharArray();

        final StringBuilder builder = new StringBuilder();

        int characterIndex = 0;

        for (final char selectedFormatChar : selectedFormatChars) {

            switch (selectedFormatChar) {
                case 'A':
                    switch (characterIndex) {
                        case 0:
                            builder.append(values(VALID_FIRST_LETTERS).next());
                            break;
                        case 1:
                            builder.append(values(VALID_SECOND_LETTERS).next());
                            break;
                        case 2:
                            builder.append(values(VALID_THIRD_LETTERS).next());
                            break;
                    }
                    break;

                case 'N':
                    builder.append(integer(0, 10).next());
                    break;
            }

            characterIndex++;
        }

        return builder.toString();
    }

}
