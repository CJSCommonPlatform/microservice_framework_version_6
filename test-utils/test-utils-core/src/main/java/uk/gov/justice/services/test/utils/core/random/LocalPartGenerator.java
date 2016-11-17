package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.generateStringFromCharacters;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

/**
 * The local name part of an email address based on the below rfc <br>
 * https://tools.ietf.org/html/rfc3696
 */
public class LocalPartGenerator extends EmailPartsGenerator {

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
    private static final char[] LOCAL_PART_STANDARD_CHARACTERS =
            "0123456789!#$%&'*+-/=?^_`.{|}~abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    /**
     * Quotes are required for the below characters <br> space, at-sign ("@"), backslash, double
     * quote, comma, colon, semi-colon, square brackets, angular brackets, parenthesis
     */
    private static final Character[] LOCAL_PART_NONSTANDARD_CHARACTERS = toObject(" \"(),:;<>@[]\\".toCharArray());

    /**
     * List of non standard characters to be preceded by a backslash
     */
    private static final String NONSTANDARD_CHARACTERS_TO_HAVE_BACKSLASH = "\"\\";

    private final Validator<String> localPartValidator;

    /**
     * Prevent instantiation outside the package
     *
     * @param localPartValidator validator for local part of the email
     */
    LocalPartGenerator(final Validator<String> localPartValidator) {
        this.localPartValidator = localPartValidator;
    }

    /**
     * Generate the local part
     *
     * @return the local part
     */
    public String next() {
        String generated = generateStringFromCharacters(LOCAL_PART_STANDARD_CHARACTERS,
                MIN_LENGTH, MAX_LENGTH);

        generated = insertOptionalNonStandardCharacterFrom(LOCAL_PART_NONSTANDARD_CHARACTERS, generated);

        if (!localPartValidator.validate(generated)) {
            return next();
        }

        return insertOptionalComment(generated);
    }

    private String insertOptionalNonStandardCharacterFrom(final Character[] nonStandardChars, final String generated) {
        String resultString = generated;
        final boolean includeNonStandardChars = BOOLEAN.next();

        if (includeNonStandardChars) {
            final int positionToInsert = RANDOM.nextInt(generated.length());
            String charToInsert = valueOf(values(nonStandardChars).next());

            if (NONSTANDARD_CHARACTERS_TO_HAVE_BACKSLASH.contains(charToInsert)) {
                charToInsert = format("\\%s", charToInsert);
            }

            resultString = format("\"%s%s%s\"", generated.substring(0, positionToInsert),
                    charToInsert, generated.substring(positionToInsert, generated.length()));
        }

        return resultString;
    }

}
