package uk.gov.justice.services.test.utils.core.random;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class LocalPartValidator implements Validator<String> {

    private static final int MAX_LENGTH = 64;
    private static final String DOT = ".";
    private static final String ALL_DIGITS_PATTERN = "\\d+";
    private static final String CONSECUTIVE_DOTS_PATTERN = ".+\\.(?=\\.).+";
    private static final String QUOTE = "\"";
    private static final String NONSTANDARD_CHARACTERS_PATTERN = ".+[ \"(),:;<>@\\[\\]\\\\].+";
    private static final String NONSTANDARD_CHARACTERS_TO_HAVE_BACKSLASH_PATTERN = ".+[\"\\\\].+";
    private static final String POSITIVE_LOOKBEHIND_FOR_BACKSLASH_PATTERN = ".+(?<=\\\\)[\"\\\\].+";

    @Override
    public boolean validate(final String localPart) {
        return isNotEmpty(localPart) && localPart.length() <= MAX_LENGTH
                && !localPart.startsWith(DOT) && !localPart.endsWith(DOT)
                && !localPart.matches(ALL_DIGITS_PATTERN)
                && (!localPart.matches(CONSECUTIVE_DOTS_PATTERN) && !localPart.matches(NONSTANDARD_CHARACTERS_PATTERN) || localPart.startsWith(QUOTE) && localPart.endsWith(QUOTE))
                && (!localPart.matches(NONSTANDARD_CHARACTERS_TO_HAVE_BACKSLASH_PATTERN) || localPart.matches(POSITIVE_LOOKBEHIND_FOR_BACKSLASH_PATTERN));
    }
}
