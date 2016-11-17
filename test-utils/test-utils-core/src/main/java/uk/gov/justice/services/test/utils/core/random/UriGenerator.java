package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.getTopLevelDomains;

import java.net.URI;
import java.net.URISyntaxException;

public class UriGenerator extends Generator<URI> {

    private final Generator<String> domainPartGenerator;
    private static final String COMMENT_PATTERN = "\\(comment\\)";

    public UriGenerator() {
        domainPartGenerator = new DomainPartGenerator(getTopLevelDomains(), new DomainPartValidator());
    }

    @Override
    public URI next() {
        final String generatedDomain = domainPartGenerator.next();
        try {
            return new URI(format("http://%s", generatedDomain.replaceAll(COMMENT_PATTERN, "")));
        } catch (final URISyntaxException e) {
            throw new RuntimeException(format("Generated URI http://%s is invalid", generatedDomain), e);
        }
    }
}
