package uk.gov.justice.services.test.utils.core.random;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;

public class UriGenerator implements Generator<URI> {
    @Override
    public URI next() {
        try {
            return new URI(format("http://%s.%s", RandomGenerator.string.next(), RandomGenerator.values("com", "co.uk", "org").next()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
