package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.maven.generator.io.files.parser.FileParser;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorParser;

import java.nio.file.Path;
import java.util.Collection;

public class SubscriptionDescriptorFileParser implements FileParser<SubscriptionDescriptor> {

    private SubscriptionDescriptorParser subscriptionDescriptorParser;

    public SubscriptionDescriptorFileParser(
            final SubscriptionDescriptorParser subscriptionDescriptorParser) {
        this.subscriptionDescriptorParser = subscriptionDescriptorParser;
    }

    @Override
    public Collection<SubscriptionDescriptor> parse(final Path baseDir, final Collection<Path> paths) {
        return paths.stream()
                .map(path -> subscriptionDescriptorParser.read(baseDir.resolve(path).toAbsolutePath()))
                .collect(toList());
    }
}
