package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.join;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.generateStringFromCharacters;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

import java.util.ArrayList;
import java.util.List;

/**
 * The domain name part of an email address has to conform to strict guidelines: <br>
 * it must match the requirements for a host name, a list of dot-separated DNS labels, <br>
 * each label being limited to a length of 63 characters and consisting of:
 * <ol>
 * <li>Upper case and lower case Latin letters A to Z and a to z</li>
 * <li>digits 0 to 9, provided that top-level domain names are not all-numeric</li>
 * <li>hyphen -, provided that it is not the first or last character</li>
 * </ol>
 *
 * Sources: <br>
 * https://tools.ietf.org/html/rfc3696 <br>
 * https://en.wikipedia.org/wiki/Email_address#domain
 */
public class DomainPartGenerator extends EmailPartsGenerator {

    /**
     * The minimum length of the domain part
     */
    private static final int MIN_LENGTH = 1;

    /**
     * The maximum length of the domain part
     */
    private static final int MAX_LENGTH = 63;

    /**
     * List of valid characters in the domain part of the email <br>
     * LDH rule ( letters digits and the hyphen )
     */
    private static final char[] DOMAIN_PART_CHARACTERS =
            "-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /**
     * Version 2016102700, Last Updated Thu Oct 27 07:07:01 2016 UTC <br>
     * http://data.iana.org/TLD/tlds-alpha-by-domain.txt
     */
    private final List<String> topLevelDomains = new ArrayList<>();
    private final Validator<String> domainPartValidator;

    /**
     * Prevent instantiation outside the package
     *
     * @param topLevelDomains     top level domains to use for generation
     * @param domainPartValidator validator for domain part
     */
    DomainPartGenerator(final List<String> topLevelDomains, final Validator<String> domainPartValidator) {
        this.topLevelDomains.addAll(topLevelDomains);
        this.domainPartValidator = domainPartValidator;
    }

    /**
     * Generate the domain part
     *
     * @return the domain part
     */
    public String next() {
        String generated = generateStringFromCharacters(DOMAIN_PART_CHARACTERS, MIN_LENGTH,
                MAX_LENGTH);
        if (!domainPartValidator.validate(generated)) {
            return next();
        }
        generated = join(".", generated, values(topLevelDomains).next());

        generated = insertOptionalComment(generated);

        return generated;
    }

    public List<String> getTopLevelDomains() {
        return topLevelDomains;
    }

}
