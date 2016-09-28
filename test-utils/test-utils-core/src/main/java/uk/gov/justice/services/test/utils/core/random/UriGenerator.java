package uk.gov.justice.services.test.utils.core.random;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

public class UriGenerator extends Generator<URI> {
    @Override
    public URI next() {
        try {
            return new URI(format("http://%s.%s", STRING.next(), values("com", "co.uk", "org").next()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
