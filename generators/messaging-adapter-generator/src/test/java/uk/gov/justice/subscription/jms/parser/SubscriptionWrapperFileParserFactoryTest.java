package uk.gov.justice.subscription.jms.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import uk.gov.justice.maven.generator.io.files.parser.FileParser;

import org.junit.Test;

public class SubscriptionWrapperFileParserFactoryTest {

    @Test
    public void shouldCreateJsonSchemaFileParser() throws Exception {

        final FileParser<SubscriptionWrapper> subscriptionDescriptorFileParser = new SubscriptionWrapperFileParserFactory().create();

        assertThat(subscriptionDescriptorFileParser, instanceOf(SubscriptionWrapperFileParser.class));
    }
}
