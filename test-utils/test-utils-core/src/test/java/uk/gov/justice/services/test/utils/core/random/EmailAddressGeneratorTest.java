package uk.gov.justice.services.test.utils.core.random;

import static com.btmatthews.hamcrest.regex.PatternMatcher.matches;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.EMAIL_ADDRESS;

import java.util.List;

import org.junit.Test;

public class EmailAddressGeneratorTest {

    private LocalPartValidator localPartValidator = new LocalPartValidator();
    private List<String> topLevelDomains = ((EmailAddressGenerator) EMAIL_ADDRESS).getDomainPartGenerator().getTopLevelDomains();

    @Test
    public void shouldGenerateValidEmails() {
        for (int i = 0; i < 10000; i++) {
            final String emailWithComments = EMAIL_ADDRESS.next();
            final String email = emailWithComments.replaceAll("\\(comment\\)", "");

            assertThat(email, containsString("@"));

            final String localPart = email.substring(0, email.lastIndexOf("@"));
            final String domainPart = email.substring(email.lastIndexOf("@") + 1, email.length());

            assertThat(format("local part validation failed for email %s", email), localPartValidator.validate(localPart), is(true));

            final String domainName = domainPart.substring(0, domainPart.indexOf('.'));
            final String topLevelDomain = domainPart.substring(domainPart.indexOf(".") + 1);

            assertThat(format("generated email %s", email), domainName, matches("[.a-zA-Z0-9-]{1,63}"));
            assertThat(format("generated email %s", email), topLevelDomain, isIn(topLevelDomains));
        }
    }
}