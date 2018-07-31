package uk.gov.justice.subscription;

import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;
import uk.gov.justice.subscription.yaml.parser.YamlToJsonObjectConverter;

import java.net.MalformedURLException;
import java.net.URL;
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
        final YamlFileValidator yamlFileValidator = new YamlFileValidator(new YamlToJsonObjectConverter(yamlParser, objectMapper), yamlSchemaLoader);

        eventSourcesParser = new EventSourcesParser(yamlParser, yamlFileValidator);
    }

    @Test
    public void shouldThrowExceptionWhenEventSourceYamlIsNotAvailable() throws Exception {
        try {

            final List<URL> urls = emptyList();
            eventSourcesParser.eventSourcesFrom(urls);;
        } catch (final YamlFileLoadingException exception) {
            assertThat(exception.getMessage(), is("No event-sources.yaml defined on the classpath"));
        }
    }

    @Test
    public void shouldParseAllEventSources() throws Exception {
        final URL url = getFromClasspath("yaml/event-sources.yaml");

        final List<EventSourceDefinition> eventSourceDefinitionsFrom = eventSourcesParser.eventSourcesFrom(singletonList(url)).collect(toList());;

        assertThat(eventSourceDefinitionsFrom.size(), is(2));
        assertThat(eventSourceDefinitionsFrom.get(0).getName(), is("people"));

        assertThat(eventSourceDefinitionsFrom.get(0).getLocation(), is(notNullValue()));
        assertThat(eventSourceDefinitionsFrom.get(0).getLocation().getJmsUri(), is("jms:topic:people.event?timeToLive=1000"));
        assertThat(eventSourceDefinitionsFrom.get(0).getLocation().getRestUri(), is("http://localhost:8080/people/event-source-api/rest"));
        assertThat(eventSourceDefinitionsFrom.get(0).getLocation().getDataSource(), is(Optional.of("java:/app/peoplewarfilename/DS.eventstore")));

        assertThat(eventSourceDefinitionsFrom.get(1).getName(), is("example"));

        assertThat(eventSourceDefinitionsFrom.get(1).getLocation(), is(notNullValue()));
        assertThat(eventSourceDefinitionsFrom.get(1).getLocation().getJmsUri(), is("jms:topic:example.event?timeToLive=1000"));
        assertThat(eventSourceDefinitionsFrom.get(1).getLocation().getRestUri(), is("http://localhost:8080/example/event-source-api/rest"));
        assertThat(eventSourceDefinitionsFrom.get(1).getLocation().getDataSource(), is(Optional.empty()));
    }


    @SuppressWarnings("ConstantConditions")
    private URL getFromClasspath(final String name) throws MalformedURLException {
        return get(getClass().getClassLoader().getResource(name).getPath()).toUri().toURL();
    }
}
