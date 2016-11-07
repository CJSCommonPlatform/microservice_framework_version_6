package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.join;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.chooseRandomPosition;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.generateStringFromCharacters;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.isRandomlyTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
 * 
 */
public class DomainPartGenerator {

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
    private static final char[] DOMAINPART_CHARACTERS =
                    "-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /**
     * Version 2016102700, Last Updated Thu Oct 27 07:07:01 2016 UTC <br>
     * http://data.iana.org/TLD/tlds-alpha-by-domain.txt
     */
    private final List<String> topLevelDomains = new ArrayList<>();

    /**
     * Random used for character generation
     */
    private final Random random;

    /**
     * Prevent instantiation outside the package
     * 
     * @param Random to use for generation
     */
    DomainPartGenerator(final Random random) {
        this.random = random;
    }

    /**
     * Generate the domain part
     * 
     * @return the domain part
     */
    public String next() {
        return generateDomainPart();
    }

    /**
     * Generate domain part
     * 
     * @return domain part
     */
    private String generateDomainPart() {
        String generated = generateStringFromCharacters(random, DOMAINPART_CHARACTERS, MIN_LENGTH,
                        MAX_LENGTH);
        if (!passBasicChecks(generated)) {
            return generateDomainPart();
        }
        // toss 100 sided thing
        if (isRandomlyTrue(100)) {
            // toss coin
            if (isRandomlyTrue(2)) {
                generated = join("", "(comment)", generated);
            } else {
                generated = join("", generated, "(comment)");
            }
        }
        generated = join(".", generated,
                        topLevelDomains.get(chooseRandomPosition(topLevelDomains.size())));

        return generated;
    }

    public List<String> getTopLevelDomains() {
        return topLevelDomains;
    }

    public DomainPartGenerator setTopLevelDomains(final List<String> topLevelDomains) {
        this.topLevelDomains.addAll(topLevelDomains);
        return this;
    }

    /**
     * Check that a domain part is valid
     * 
     * @param textToCheck
     * @return boolean value denoting a valid or invalid domain part
     */
    public static boolean passBasicChecks(final String textToCheck) {
        return !textToCheck.isEmpty() && !(MAX_LENGTH < textToCheck.length())
                        && !textToCheck.startsWith("-") && !textToCheck.endsWith("-")
                        && !textToCheck.matches("[0-9]+");
    }

}
