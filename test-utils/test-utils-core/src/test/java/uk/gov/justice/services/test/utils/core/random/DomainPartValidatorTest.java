package uk.gov.justice.services.test.utils.core.random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.string;

import org.junit.Test;

public class DomainPartValidatorTest {

    private final DomainPartValidator domainPartValidator = new DomainPartValidator();

    @Test
    public void shouldPassValidationWhenDomainPartIsValid() {
        assertThat(domainPartValidator.validate("hmcts.net"), is(true));
    }

    @Test
    public void shouldFailValidationWhenDomainPartStartsWithHyphen() {
        assertThat(domainPartValidator.validate("-test.co.uk"), is(false));
    }

    @Test
    public void shouldFailValidationWhenDomainPartEndsWithHyphen() {
        assertThat(domainPartValidator.validate("test.co.uk-"), is(false));
    }

    @Test
    public void shouldFailValidationWhenDomainPartIsNotAllNumeric() {
        assertThat(domainPartValidator.validate("1234567"), is(false));
    }

    @Test
    public void shouldFailValidationWhenDomainPartIsNullOrEmpty() {
        assertThat(domainPartValidator.validate(null), is(false));
        assertThat(domainPartValidator.validate(""), is(false));
    }

    @Test
    public void shouldFailValidationWhenDomainPartIsGreaterThan63Characters() {
        assertThat(domainPartValidator.validate(string(64).next()), is(false));
    }
}