package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.getTopLevelDomains;

/**
 * Class to generate email addresses <br>
 * <br>
 * The rules are derived from the below RFC <br>
 * https://tools.ietf.org/html/rfc3696 <br>
 * Top Level Domains <br>
 * http://data.iana.org/TLD/tlds-alpha-by-domain.txt <br>
 */
public class EmailAddressGenerator extends Generator<String> {

    private final LocalPartGenerator localPartGenerator;
    private final DomainPartGenerator domainPartGenerator;

    public EmailAddressGenerator() {
        localPartGenerator = new LocalPartGenerator(new LocalPartValidator());
        domainPartGenerator = new DomainPartGenerator(getTopLevelDomains(), new DomainPartValidator());
    }

    public LocalPartGenerator getLocalPartGenerator() {
        return localPartGenerator;
    }

    public DomainPartGenerator getDomainPartGenerator() {
        return domainPartGenerator;
    }

    @Override
    public String next() {
        return format("%s@%s", localPartGenerator.next(), domainPartGenerator.next());
    }

}
