package uk.gov.justice.subscription.jms.parser;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class PathToUrlResolver {

    public URL resolveToUrl(final Path baseDir, final Path path) {
        try {
            return baseDir.resolve(path).toUri().toURL();
        } catch (final MalformedURLException e) {
            throw new FileParserException(format("Cannot resolve path as URL %s", path), e);
        }
    }
}
