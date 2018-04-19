package uk.gov.justice.subscription;

import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.yaml.parser.YamlFileToJsonObjectConverter;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;


public class EventSourcesParserTest {

    private EventSourcesParser eventSourcesParser;

    @Before
    public void setUp() {
        final YamlParser yamlParser = new YamlParser();
        final YamlSchemaLoader yamlSchemaLoader = new YamlSchemaLoader();
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final YamlFileValidator yamlFileValidator = new YamlFileValidator(new YamlFileToJsonObjectConverter(yamlParser, objectMapper), yamlSchemaLoader);

        eventSourcesParser = new EventSourcesParser(yamlParser, yamlFileValidator);
    }

    @Test
    public void shouldRetrieveAllEventSources() throws Exception {
        final Path path = getFromClasspath("event-sources.yaml");

        final List<EventSource> eventSourcesFrom = eventSourcesParser.getEventSourcesFrom(asList(path)).collect(toList());

        assertThat(eventSourcesFrom.size(), is(2));
        assertThat(eventSourcesFrom.get(0).getName(), is("people"));

        assertThat(eventSourcesFrom.get(0).getLocation(), is(notNullValue()));
        assertThat(eventSourcesFrom.get(0).getLocation().getJmsUri(), is("jms:topic:people.event?timeToLive=1000"));
        assertThat(eventSourcesFrom.get(0).getLocation().getRestUri(), is("http://localhost:8080/people/event-source-api/rest"));
        assertThat(eventSourcesFrom.get(0).getLocation().getDataSource(), is(Optional.of("java:/app/peoplewarfilename/DS.eventstore")));

        assertThat(eventSourcesFrom.get(1).getName(), is("example"));

        assertThat(eventSourcesFrom.get(1).getLocation(), is(notNullValue()));
        assertThat(eventSourcesFrom.get(1).getLocation().getJmsUri(), is("jms:topic:example.event?timeToLive=1000"));
        assertThat(eventSourcesFrom.get(1).getLocation().getRestUri(), is("http://localhost:8080/example/event-source-api/rest"));
        assertThat(eventSourcesFrom.get(1).getLocation().getDataSource(), is(Optional.empty()));
    }


    @SuppressWarnings("ConstantConditions")
    private Path getFromClasspath(final String name) {
        return get(getClass().getClassLoader().getResource(name).getPath());
    }
}