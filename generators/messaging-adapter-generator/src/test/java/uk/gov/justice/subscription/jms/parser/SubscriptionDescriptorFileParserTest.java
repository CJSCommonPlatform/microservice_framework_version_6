package uk.gov.justice.subscription.jms.parser;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorParser;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDescriptorFileParserTest {

    @Mock
    private SubscriptionDescriptorParser subscriptionDescriptorParser;

    @InjectMocks
    private SubscriptionDescriptorFileParser subscriptionDescriptorFileParser;


    @Test
    public void shouldParseEverySubsctiptionYamlFileIntoASubscriptionDescriptor() throws Exception {

        final Path baseDir = mock(Path.class, RETURNS_DEEP_STUBS.get());

        final Path subscriptionFile_1 = mock(Path.class);
        final Path subscriptionFile_2 = mock(Path.class);

        final Path filePath_1 = mock(Path.class);
        final Path filePath_2 = mock(Path.class);

        final SubscriptionDescriptor subscriptionDescriptor_1 = mock(SubscriptionDescriptor.class);
        final SubscriptionDescriptor subscriptionDescriptor_2 = mock(SubscriptionDescriptor.class);

        when(baseDir.resolve(subscriptionFile_1).toAbsolutePath()).thenReturn(filePath_1);
        when(baseDir.resolve(subscriptionFile_2).toAbsolutePath()).thenReturn(filePath_2);

        when(subscriptionDescriptorParser.read(filePath_1)).thenReturn(subscriptionDescriptor_1);
        when(subscriptionDescriptorParser.read(filePath_2)).thenReturn(subscriptionDescriptor_2);

        final Collection<SubscriptionDescriptor> subscriptionDescriptors = subscriptionDescriptorFileParser.parse(
                baseDir,
                asList(subscriptionFile_1, subscriptionFile_2)
        );

        assertThat(subscriptionDescriptors.size(), is(2));

        assertThat(subscriptionDescriptors, hasItem(subscriptionDescriptor_1));
        assertThat(subscriptionDescriptors, hasItem(subscriptionDescriptor_2));
    }
}
