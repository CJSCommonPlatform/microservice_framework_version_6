package uk.gov.justice.subscription.jms.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.maven.generator.io.files.parser.FileParser;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;

import org.junit.Test;

public class SubscriptionDescriptorFileParserFactoryTest {

    @Test
    public void shouldCreateJsonSchemaFileParser() throws Exception {

        final FileParser<SubscriptionDescriptor> subscriptionDescriptorFileParser = new SubscriptionDescriptorFileParserFactory().create();

        assertThat(subscriptionDescriptorFileParser, is(instanceOf(SubscriptionDescriptorFileParser.class)));
    }
}
