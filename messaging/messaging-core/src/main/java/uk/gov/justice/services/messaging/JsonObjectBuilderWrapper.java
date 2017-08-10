package uk.gov.justice.services.messaging;


import static javax.json.Json.createObjectBuilder;
import static javax.json.JsonValue.ValueType.OBJECT;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@Deprecated
class JsonObjectBuilderWrapper {

    private JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
    private final Map<String, Object> entryMap = new HashMap<>();

    @Deprecated
    public JsonObjectBuilderWrapper(final JsonObject jsonObject) {
        jsonObject.forEach((k, v) -> {
            if (v.getValueType() == OBJECT) {
                entryMap.put(k, JsonObjects.createObjectBuilder((JsonObject) v));
            }
        });
        jsonObjectBuilder = JsonObjects.createObjectBuilder(jsonObject);
    }

    @Deprecated
    public JsonObjectBuilderWrapper() {
        jsonObjectBuilder = createObjectBuilder();

    }

    void add(final JsonArrayBuilder value, final String name) {
        entryMap.put(name, value);
    }

    void add(final BigDecimal value, final String... name) {
        final JsonObjectBuilder jsonObject = addOrReturnNestedJsonObject(value, name);
        if (jsonObject != null) {
            jsonObject.add(name[1], value);
        }
    }

    void add(final Integer value, final String... name) {
        final JsonObjectBuilder jsonObject = addOrReturnNestedJsonObject(value, name);
        if (jsonObject != null) {
            jsonObject.add(name[1], value);
        }
    }

    void add(final Boolean value, final String... name) {
        final JsonObjectBuilder jsonObject = addOrReturnNestedJsonObject(value, name);
        if (jsonObject != null) {
            jsonObject.add(name[1], value);
        }
    }

    void add(final String value, final String... name) {
        final JsonObjectBuilder jsonObject = addOrReturnNestedJsonObject(value, name);
        if (jsonObject != null) {
            jsonObject.add(name[1], value);
        }
    }

    void add(final JsonObject value, final String... name) {
        final JsonObjectBuilder jsonObject = addOrReturnNestedJsonObject(value, name);
        if (jsonObject != null) {
            jsonObject.add(name[1], value);
        }
    }

    private JsonObjectBuilder addOrReturnNestedJsonObject(final Object value, final String[] name) {
        JsonObjectBuilder nestedJsonObject = null;
        switch (name.length) {
            case 1:
                entryMap.put(name[0], value);
                break;
            case 2:
                nestedJsonObject = nestedJsonObjectOf(name[0]);
                break;
            default:
                throw new IllegalArgumentException("Only 1 level of nested json elements supported");

        }
        return nestedJsonObject;
    }

    private JsonObjectBuilder nestedJsonObjectOf(final String name) {
        final Object object = entryMap.get(name);

        JsonObjectBuilder nestedJsonObject;

        if (object == null) {
            nestedJsonObject = createObjectBuilder();
            entryMap.put(name, nestedJsonObject);
        } else {
            nestedJsonObject = (JsonObjectBuilder) object;
        }
        return nestedJsonObject;
    }

    JsonObject build() {

        entryMap.forEach((name, value) -> {
            ValueType type = ValueType.valueOf(value.getClass());
            switch (type) {
                case JsonObjectBuilder:
                    jsonObjectBuilder.add(name, ((JsonObjectBuilder) value).build());
                    break;
                case JsonArrayBuilder:
                    jsonObjectBuilder.add(name, ((JsonArrayBuilder) value).build());
                    break;
                case BigDecimal:
                    jsonObjectBuilder.add(name, (BigDecimal) value);
                    break;
                case Integer:
                    jsonObjectBuilder.add(name, (Integer) value);
                    break;
                case JsonObject:
                    jsonObjectBuilder.add(name, (JsonObject) value);
                    break;
                case Boolean:
                    jsonObjectBuilder.add(name, (Boolean) value);
                    break;
                default:
                    jsonObjectBuilder.add(name, String.valueOf(value));
            }
        });
        return jsonObjectBuilder.build();
    }

    enum ValueType {
        String, BigDecimal, Integer, Boolean, JsonObject, JsonObjectBuilder, JsonArrayBuilder;

        static ValueType valueOf(Class<?> clazz) {
            final String className = clazz.getSimpleName();
            ValueType valueType = valueTypeOf(className);
            if (valueType == null) {
                for (Class<?> interfaceClazz : clazz.getInterfaces()) {
                    valueType = valueTypeOf(interfaceClazz.getSimpleName());
                    if (valueType != null) {
                        return valueType;
                    }
                }
            }
            return valueType;
        }

        private static ValueType valueTypeOf(final String className) {
            for (ValueType t : ValueType.values()) {
                if (t.name().equals(className)) {
                    return t;
                }
            }
            return null;
        }
    }

}
