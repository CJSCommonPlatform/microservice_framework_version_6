package uk.gov.justice.subscription;

import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.yaml.YamlFileValidator;
import uk.gov.justice.services.yaml.YamlParser;
import uk.gov.justice.services.yaml.YamlSchemaLoader;
import uk.gov.justice.services.yaml.YamlToJsonObjectConverter;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionsDescriptorParserTest {

    private SubscriptionsDescriptorParser subscriptionsDescriptorParser;

    @Before
    public void setUp() {
        final YamlParser yamlParser = new YamlParser();
        final YamlSchemaLoader yamlSchemaLoader = new YamlSchemaLoader();
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final YamlFileValidator yamlFileValidator = new YamlFileValidator(new YamlToJsonObjectConverter(yamlParser, objectMapper), yamlSchemaLoader);
        final SubscriptionSorter subscriptionSorter = new SubscriptionSorter();

        subscriptionsDescriptorParser = new SubscriptionsDescriptorParser(yamlParser, yamlFileValidator, subscriptionSorter);
    }

    @Test
    public void shouldParseSubscriptionDescriptorYamlUrlAndReturnInPriorityOrder() throws Exception {
        final URL url_1 = getFromClasspath("yaml/subscriptions-descriptor.yaml");
        final URL url_2 = getFromClasspath("yaml/no-priority-subscriptions-descriptor.yaml");
        final URL url_3 = getFromClasspath("yaml/priority-one-subscriptions-descriptor.yaml");

        final List<SubscriptionsDescriptor> subscriptionsDescriptors = subscriptionsDescriptorParser
                .getSubscriptionDescriptorsFrom(asList(url_1, url_2, url_3))
                .collect(toList());

        assertThat(subscriptionsDescriptors.size(), is(3));
        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptors.get(0);
        assertThat(subscriptionsDescriptor_1.getSubscriptions().size(), is(1));
        assertThat(subscriptionsDescriptor_1.getService(), is("examplecontext"));
        assertThat(subscriptionsDescriptor_1.getServiceComponent(), is("NO_PRIORITY_EVENT_LISTENER"));
        assertThat(subscriptionsDescriptor_1.getSpecVersion(), is("1.0.0"));
        assertThat(subscriptionsDescriptor_1.getPrioritisation(), is(0));

        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptors.get(1);
        assertThat(subscriptionsDescriptor_2.getSubscriptions().size(), is(1));
        assertThat(subscriptionsDescriptor_2.getService(), is("examplecontext"));
        assertThat(subscriptionsDescriptor_2.getServiceComponent(), is("EVENT_PROCESSOR"));
        assertThat(subscriptionsDescriptor_2.getSpecVersion(), is("1.0.0"));
        assertThat(subscriptionsDescriptor_2.getPrioritisation(), is(1));

        final SubscriptionsDescriptor subscriptionsDescriptor_3 = subscriptionsDescriptors.get(2);
        assertThat(subscriptionsDescriptor_3.getSubscriptions().size(), is(2));
        assertThat(subscriptionsDescriptor_3.getService(), is("examplecontext"));
        assertThat(subscriptionsDescriptor_3.getServiceComponent(), is("EVENT_LISTENER"));
        assertThat(subscriptionsDescriptor_3.getSpecVersion(), is("1.0.0"));
        assertThat(subscriptionsDescriptor_3.getPrioritisation(), is(2));
    }

    @SuppressWarnings("ConstantConditions")
    private URL getFromClasspath(final String name) throws MalformedURLException {
        return get(getClass().getClassLoader().getResource(name).getPath()).toUri().toURL();
    }
}
