package uk.gov.justice.services.test.utils.domain;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.io.Resources.getResource;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import org.reflections.Reflections;

public class AggregateUnderTest {
    private static final String NAME = "name";
    private static final String METADATA = "_metadata";
    private static final Reflections UK_GOV_REFLECTIONS = new Reflections("uk.gov");
    private static final Set<Class<?>> EVENT_ANNOTATED_CLASSES = UK_GOV_REFLECTIONS.getTypesAnnotatedWith(Event.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperProducer().objectMapper();
    private List<Object> initialEvents = new LinkedList<>();
    private final List<Object> generatedEvents = new LinkedList<>();
    Aggregate object;

    public static AggregateUnderTest aggregateUnderTest() {
        return new AggregateUnderTest();
    }

    public AggregateUnderTest withInitialEventsFromFiles(final String fileNames) throws IOException, ClassNotFoundException {

        this.initialEvents = eventsFromFileNames(fileNames);
        return this;
    }

    private List<Object> eventsFromFileNames(final String fileNames) {
        return jsonNodesStreamFrom(fileNames)
                .map(jsonEvent -> eventObjectFrom(jsonEvent, eventClassFrom(jsonEvent)))
                .collect(toList());
    }

    public static Stream<JsonNode> jsonNodesStreamFrom(final String fileNames) {
        return stream(fileNames.split(","))
                .map(fileName -> jsonNodeFrom(fileName));
    }



    public static JsonNode jsonNodeWithoutMetadataFrom(final JsonNode jsonEvent) {
        final JsonNode jsonEventCopy = jsonEvent.deepCopy();
        ((ObjectNode) jsonEventCopy).remove(METADATA);
        return jsonEventCopy;
    }

    public AggregateUnderTest initialiseFromClass(final String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (object == null) {
            this.object = (Aggregate) Class.forName(className).newInstance();
            this.object.apply(initialEvents.stream());
        }
        return this;
    }

    public void invokeMethod(final String methodName, final String fileContainingArguments) throws Exception {

        final String json = jsonStringFrom(fileContainingArguments);
        final Set<String> suppliedParamNames = getParamNames(json);
        final Method method = getMethod(object.getClass(), methodName, suppliedParamNames);

        final Object[] args = getArgs(json, method);
        final List<Object> newEvents = ((Stream<Object>) method.invoke(object, args)).collect(toList());
        generatedEvents.addAll(newEvents);
    }

    private Set<String> getParamNames(final String json) throws IOException {
        final JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
        return newHashSet(jsonNode.fieldNames());
    }

    private Method getMethod(final Class<?> clazz, final String methodName, final Set<String> suppliedParamNames) {
        final List<Method> methods = stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> suppliedParamNames.equals(
                        stream(m.getParameters())
                                .map(Parameter::getName)
                                .collect(toSet())))
                .collect(toList());

        if (methods.size() == 0) {
            throw new IllegalArgumentException("No method found");
        } else if (methods.size() > 1 ) {
            throw new IllegalArgumentException("Too many matching methods found");
        }

        return methods.get(0);
    }

    private Object[] getArgs(final String json, final Method target) throws IOException {
        final JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
        return stream(target.getParameters())
                .map(p -> OBJECT_MAPPER.convertValue(jsonNode.get(p.getName()), p.getType()))
                .collect(toList())
                .toArray();
    }

    private static JsonNode jsonNodeFrom(final String fileName) {
        try {
            return OBJECT_MAPPER.readTree(jsonStringFrom(fileName));
        } catch (IOException e) {
            throw new IllegalArgumentException(format("Error parsing json file %s", fileName), e);
        }
    }

    private Class<?> eventClassFrom(final JsonNode jsonEvent) {
        final String eventName = eventNameFrom(jsonEvent);
        return EVENT_ANNOTATED_CLASSES.stream()
                .filter(clazz -> clazz.getAnnotation(Event.class).value().equalsIgnoreCase(eventName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(format("Error applying initial event: %s. Event class not found", eventName)));

    }

    private static Object eventObjectFrom(final JsonNode jsonEvent, final Class<?> clazz) {
        try {
            return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(jsonNodeWithoutMetadataFrom(jsonEvent)), clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(format("Error instantiating event %s", clazz), e);
        }
    }

    private static String jsonStringFrom(final String fileName) {
        try {
            return Resources.toString(getResource(format("json/%s.json", fileName)), defaultCharset());
        } catch (final Exception e) {
            throw new IllegalArgumentException(format("Error reading json file: %s", fileName), e);
        }
    }

    private String eventNameFrom(Object obj) {
        return obj.getClass().getAnnotation(Event.class).value();
    }

    public static String eventNameFrom(JsonNode node) {
        return node.get(METADATA).path(NAME).asText();
    }

    public List<Object> generatedEvents() {
        return generatedEvents;
    }

    public String generatedEventName(final int index) {
        return eventNameFrom(generatedEvents.get(index));
    }


    public JsonNode generatedEventAsJsonNode(final int index) {
        return OBJECT_MAPPER.valueToTree(generatedEvents.get(index));
    }
}
