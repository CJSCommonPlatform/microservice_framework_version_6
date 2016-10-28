package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;

import java.io.IOException;

/**
 * 
 * Class to generate email addresses <br>
 * <br>
 * The rules are derived from the below RFC <br>
 * https://tools.ietf.org/html/rfc3696 <br>
 * Top Level Domains <br>
 * http://data.iana.org/TLD/tlds-alpha-by-domain.txt <br>
 * 
 */
public class EmailAddressGenerator extends Generator<String> {


    private final LocalPartGenerator localPartGenerator;
    private final DomainPartGenerator domainPartGenerator;

    /**
     * Package only access
     * 
     * @throws IOException
     * 
     * @see RandomGenerator
     */
    private EmailAddressGenerator() {
        localPartGenerator = new LocalPartGenerator(RANDOM);
        domainPartGenerator = new DomainPartGenerator(RANDOM);
    }
    
    public static EmailAddressGenerator getInstance(){
        final EmailAddressGenerator generator = new EmailAddressGenerator();
        generator.getDomainPartGenerator().setTopLevelDomains(GeneratorUtil.getTopLevelDomains());
        
        return generator;
    }
    
    public LocalPartGenerator getLocalPartGenerator(){
        return localPartGenerator;
    }
    
    public DomainPartGenerator getDomainPartGenerator(){
        return domainPartGenerator;
    }

    @Override
    public String next() {
        return format("%s@%s", localPartGenerator.next(), domainPartGenerator.next());
    }

}
