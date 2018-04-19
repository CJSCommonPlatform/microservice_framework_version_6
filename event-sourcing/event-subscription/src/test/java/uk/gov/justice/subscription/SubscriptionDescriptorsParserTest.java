package uk.gov.justice.subscription;

import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.yaml.parser.YamlFileToJsonObjectConverter;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionDescriptorsParserTest {

    private SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    @Before
    public void setUp() {
        final YamlParser yamlParser = new YamlParser();
        final YamlSchemaLoader yamlSchemaLoader = new YamlSchemaLoader();
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final YamlFileValidator yamlFileValidator = new YamlFileValidator(new YamlFileToJsonObjectConverter(yamlParser, objectMapper), yamlSchemaLoader);

        subscriptionDescriptorsParser = new SubscriptionDescriptorsParser(yamlParser, yamlFileValidator);
    }

    @Test
    public void shouldName() throws Exception {
        final Path path = getFromClasspath("subscription-descriptor.yaml");
        final List<SubscriptionDescriptor> subscriptionDescriptorList = subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(asList(path)).collect(toList());

        assertThat(subscriptionDescriptorList.size(), is(1));
        assertThat(subscriptionDescriptorList.get(0).getSubscriptions().size(), is(2));
        assertThat(subscriptionDescriptorList.get(0).getService(), is("examplecontext"));
        assertThat(subscriptionDescriptorList.get(0).getServiceComponent(), is("EVENT_LISTENER"));
        assertThat(subscriptionDescriptorList.get(0).getSpecVersion(), is("1.0.0"));
    }

    @SuppressWarnings("ConstantConditions")
    private Path getFromClasspath(final String name) {
        return get(getClass().getClassLoader().getResource(name).getPath());
    }
}