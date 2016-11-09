package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;

public abstract class EmailPartsGenerator extends Generator<String> {

    private static final String COMMENT = "(comment)";

    protected String insertOptionalComment(final String emailPart) {
        String resultString = emailPart;
        final boolean includeComment = BOOLEAN.next();
        final boolean appendComment = BOOLEAN.next();

        if (includeComment) {
            if (appendComment) {
                resultString = format("%s%s", emailPart, COMMENT);
            } else {
                resultString = format("%s%s", COMMENT, emailPart);
            }
        }
        return resultString;
    }
}
