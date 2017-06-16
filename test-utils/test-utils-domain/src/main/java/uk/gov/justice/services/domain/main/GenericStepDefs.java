package uk.gov.justice.services.domain.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

public class GenericStepDefs {
    private static final String METADATA = "_metadata";
    private static final String NAME = "name";
    private List givenEvents = new ArrayList();
    private List generatedEvents = new ArrayList();
    private Class<?> clazz;
    private Aggregate object;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static Class classWithFullyQualifiedClassName(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            final Package[] packages = Package.getPackages();
            for (final Package p : packages) {
                final String pack = p.getName();
                final String tentative = pack + "." + StringUtils.capitalize(className);
                try {
                    clazz = Class.forName(tentative);
                } catch (final ClassNotFoundException exception) {
                    continue;
                }
                break;
            }
        }
        return clazz;
    }

    @Given("no previous events")
    public void no_previous_events() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        assert givenEvents.size() == 0;
    }

    @Given("there are previous events (.*)")
    public void previous_events(final String fileNames) throws Exception {
        String filesNames[] = fileNames.split(",");
        for (String fileName : filesNames) {
            String message = json(fileName);
            ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
            final JsonNode fromJson = mapper.readTree(message);
            Reflections reflections = new Reflections("uk.gov");
            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Event.class);
            for (Class<?> clazz : annotated) {
                if (clazz.getAnnotation(Event.class).value().equalsIgnoreCase(eventName(fromJson))) {
                    removeMetaDataNode(fromJson);
                    generatedEvents.add(mapper.readValue(mapper.writeValueAsString(fromJson), clazz));
                    break;
                }
            }
        }
    }

    @When("(.*) to a (.*) using (.*)")
    public void call_method_with_params(final String methodName, final String aggregate, final String fileName)
            throws Exception {
        createAggregate(aggregate);
        ObjectMapper mapper = new ObjectMapper();//normal mapper to get the map value in correct order
        Map argumentsMap = mapper.convertValue(mapper.readTree(json(fileName)), Map.class);
        mapper = new ObjectMapperProducer().objectMapper();
        List valuesList = new ArrayList(argumentsMap.values());
        checkIfUUID(valuesList);
        Method method = object.getClass().getMethod(methodName, paramsTypes(methodName));
        if (argumentsMap.size() == 0) {
            givenEvents = ((Stream<Object>) method.invoke(object, null)).collect(Collectors.toList());
            object.apply(givenEvents.stream());
        } else {
            givenEvents = ((Stream<Object>) method.invoke(object,
                    methodArgs(valuesList, getAllEventNames(argumentsMap),
                            mapper))).
                    collect(Collectors.toList());
            object.apply(givenEvents.stream());
        }
    }

    @Then("the (.*)")
    public void new_recipe_event_generated(final String fileNames) throws ClassNotFoundException,
            IOException, IllegalAccessException, InstantiationException {
        String filesNames[] = fileNames.split(",");
        for (int index = 0; index < filesNames.length; index++) {
            String message = json(filesNames[index]);
            ObjectMapper mapper = mapper();
            final JsonNode fromJson = mapper.readTree(message);
            assertEquals(filesNames.length, givenEvents.size());
            assertTrue(fromJson.get(METADATA).get(NAME).asText().equalsIgnoreCase(eventName(givenEvents.get(index))));
            removeMetaDataNode(fromJson);//remove metadata node to compare two json objects
            assertTrue(fromJson.equals(mapper.valueToTree(givenEvents.get(index))));
        }
    }

    private String json(String file) throws IOException {
        try {
            return Resources.toString(Resources.getResource("json/" + file + ".json"), Charset.defaultCharset());
        } catch (Exception e) {
            fail("Error consuming file from location " + file);
        }
        return null;
    }

    private Object[] methodArgs(List valuesList, List<String> expectedEventNames, ObjectMapper mapper) throws Exception {
        Object[] objects = new Object[valuesList.size()];
        int objectIndexInJson = 0;
        for (int index = 0; index < valuesList.size(); index++) {
            if (valuesList.get(index) instanceof HashMap) {
                objects[index] = mapper.readValue(mapper.writeValueAsString(valuesList.get(index)),
                        classWithFullyQualifiedClassName(expectedEventNames.get(objectIndexInJson)));
                objectIndexInJson = objectIndexInJson + 1;
            } else {
                objects[index] = valuesList.get(index);
            }
        }
        return objects;
    }

    private void removeMetaDataNode(JsonNode node) {
        ObjectNode object = (ObjectNode) node;
        object.remove(METADATA);
    }

    private List<String> getAllEventNames(Map argumentsMap) {
        List<String> classNames = new ArrayList();
        Iterator it = argumentsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue() instanceof HashMap) {
                classNames.add((String) pair.getKey());
            }
        }
        return classNames;
    }

    private String eventName(JsonNode node) {
        return node.get(METADATA).path(NAME).asText();
    }

    private String eventName(Object obj) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(obj.getClass().getName());
        return clazz.getAnnotation(Event.class).value();
    }

    private ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private void createAggregate(final String aggregate) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (clazz == null && object == null) {
            this.clazz = classWithFullyQualifiedClassName(aggregate);
            this.object = (Aggregate) clazz.newInstance();
        }
        object.apply(generatedEvents.stream());
    }

    private Class<?>[] paramsTypes(final String methodName) {
        Class<?>[] paramsTypes = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            paramsTypes = m.getParameterTypes();
        }
        return paramsTypes;
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
}