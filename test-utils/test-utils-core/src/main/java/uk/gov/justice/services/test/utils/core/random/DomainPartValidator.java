package uk.gov.justice.services.test.utils.core.random;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class DomainPartValidator implements Validator<String> {

    private static final int MAX_LENGTH = 63;
    private static final String ALL_DIGITS_PATTERN = "\\d+";
    private static final String HYPHEN = "-";

    @Override
    public boolean validate(final String domainPart) {
        return isNotEmpty(domainPart) && domainPart.length() <= MAX_LENGTH
                && !domainPart.startsWith(HYPHEN) && !domainPart.endsWith(HYPHEN)
                && !domainPart.matches(ALL_DIGITS_PATTERN);
    }
}
