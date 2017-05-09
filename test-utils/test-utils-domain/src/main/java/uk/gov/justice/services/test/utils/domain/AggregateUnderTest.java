package uk.gov.justice.services.test.utils.domain;

import static com.google.common.io.Resources.getResource;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
        ObjectMapper mapper = new ObjectMapper();//normal mapper to get the map value in correct order
        final JsonNode jsonNode = mapper.readTree(jsonStringFrom(fileContainingArguments));
        final Map<String, Object> argumentsMap = mapper.convertValue(jsonNode, Map.class);

        List<Object> valuesList = new ArrayList(argumentsMap.values());
        checkIfUUID(valuesList);
        Method method = object.getClass().getMethod(methodName, paramsTypes(methodName));
        if (argumentsMap.size() == 0) {
            final List<Object> newEvents = ((Stream<Object>) method.invoke(object, null)).collect(toList());
            this.generatedEvents.addAll(newEvents);
        } else {
            final Object[] args = methodArgs(valuesList, getAllEventNames(argumentsMap), OBJECT_MAPPER);
            final List<Object> newEvents = ((Stream<Object>) method.invoke(object, args)).collect(toList());
            this.generatedEvents.addAll(newEvents);

        }

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

    private void checkIfUUID(final List argumentValues) {
        for (int index = 0; index < argumentValues.size(); index++) {
            try {
                if (argumentValues.get(index) instanceof String) {
                    UUID uuid = UUID.fromString((String) argumentValues.get(index));
                    argumentValues.remove(index);
                    argumentValues.add(index, uuid);
                }
            } catch (IllegalArgumentException exception) {
                continue;
            }
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

    private Object[] methodArgs(List<Object> valuesList, List<String> expectedEventNames, ObjectMapper mapper) throws Exception {
        Object[] objects = new Object[valuesList.size()];
        int objectIndexInJson = 0;
        for (int index = 0; index < valuesList.size(); index++) {
            if (valuesList.get(index) instanceof HashMap) {
                objects[index] = mapper.readValue(mapper.writeValueAsString(valuesList.get(index)),
                        classOf(expectedEventNames.get(objectIndexInJson)));
                objectIndexInJson = objectIndexInJson + 1;
            } else {
                objects[index] = valuesList.get(index);
            }
        }
        return objects;
    }

    private Class<?>[] paramsTypes(final String methodName) {
        Class<?>[] paramsTypes = null;
        for (Method m : this.object.getClass().getDeclaredMethods()) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            paramsTypes = m.getParameterTypes();
        }
        return paramsTypes;
    }

    private List<String> getAllEventNames(Map<String, Object> argumentsMap) {
        final List<String> classNames = new ArrayList();
        final Iterator it = argumentsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue() instanceof HashMap) {
                classNames.add((String) pair.getKey());
            }
        }
        return classNames;
    }

    private static Class classOf(final String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            final Package[] packages = Package.getPackages();
            for (final Package p : packages) {
                try {
                    clazz = Class.forName(new StringBuilder().append(p.getName()).append(".").append(capitalize(className)).toString());
                } catch (final ClassNotFoundException exception) {
                    continue;
                }
                break;
            }
        }
        return clazz;
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
