package uk.gov.justice.services.messaging;

import static javax.json.JsonValue.ValueType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.google.common.collect.ImmutableList;

/**
 * Collection of static utility methods for getting deep values from a {@link JsonObject}.
 */
public final class JsonObjects {

    /**
     * Private constructor to prevent misuse of utility class.
     */
    private JsonObjects() {
    }

    /**
     * Returns the (possibly nested) array value to which the specified name is mapped, if it
     * exists.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  the field name path whose associated value is to be returned
     * @return the array value to which the specified name is mapped
     * @throws IllegalStateException if the value is not assignable to JsonArray type
     */
    public static Optional<JsonArray> getJsonArray(final JsonObject object, final String... names) {
        return getJsonValue(object, ValueType.ARRAY, JsonObject::getJsonArray, names);
    }

    /**
     * Returns the (possibly nested) object value to which the specified name is mapped, if it
     * exists.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  the field name path whose associated value is to be returned
     * @return the object value to which the specified name is mapped, or {@code null} if this
     * object contains no mapping for the name
     * @throws IllegalStateException if the value is not assignable to JsonObject type
     */
    public static Optional<JsonObject> getJsonObject(final JsonObject object, final String... names) {
        return getJsonValue(object, ValueType.OBJECT, JsonObject::getJsonObject, names);
    }

    /**
     * Returns the (possibly nested) number value to which the specified name is mapped, if it
     * exists.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  the field name path whose associated value is to be returned
     * @return the number value to which the specified name is mapped
     * @throws IllegalStateException if the value is not assignable to JsonNumber type
     */
    public static Optional<JsonNumber> getJsonNumber(final JsonObject object, final String... names) {
        return getJsonValue(object, ValueType.NUMBER, JsonObject::getJsonNumber, names);
    }

    /**
     * Returns the (possibly nested) string value to which the specified name is mapped, if it
     * exists.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  the field name path whose associated value is to be returned
     * @return the string value to which the specified name is mapped
     * @throws IllegalStateException if the value is not assignable to JsonString type
     */
    public static Optional<JsonString> getJsonString(final JsonObject object, final String... names) {
        return getJsonValue(object, ValueType.STRING, JsonObject::getJsonString, names);
    }

    /**
     * Returns a (possibly nested) value converted into a specified JsonValue type.
     *
     * @param object    the JsonObject from which to retrieve the value
     * @param valueType the type of JsonValue we need to return
     * @param function  the function to use to get the correct type of JsonValue from the
     *                  JsonObject
     * @param names     the field name path whose associated value is to be returned
     * @param <T>       the type of JsonValue that will be returned
     * @return an optional value found at the specified location in the JsonObject
     */
    private static <T extends JsonValue> Optional<T> getJsonValue(final JsonObject object,
                                                                  final ValueType valueType,
                                                                  final BiFunction<JsonObject, String, T> function,
                                                                  final String... names) {
        checkArguments(object, names);
        if (names.length == 1) {
            if (!object.containsKey(names[0])) {
                return Optional.empty();
            }
            ValueType actualValueType = object.get(names[0]).getValueType();

            if (ValueType.NULL.equals(actualValueType)) {
                return Optional.empty();
            }

            if (!valueType.equals(actualValueType)) {
                throw new IllegalStateException(String.format("Field %s is not a %s", names[0], valueType.toString()));
            }

            return Optional.of(function.apply(object, names[0]));
        } else {
            return getJsonObject(object, names[0])
                    .flatMap(subObject -> getJsonValue(subObject, valueType, function, Arrays.copyOfRange(names, 1, names.length)));
        }
    }

    /**
     * A convenience method for {@code getJsonString(name).get().getString()}.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  whose associated value is to be returned as String
     * @return the String value to which the specified name is mapped
     * @throws IllegalStateException if the value is not assignable to JsonString type
     */
    public static Optional<String> getString(final JsonObject object, final String... names) {
        return getJsonValue(object, ValueType.STRING, JsonObject::getJsonString, names)
                .map(JsonString::getString);
    }

    /**
     * A convenience method for {@code UUID.fromString(getJsonString(name).get().getString())}.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  whose associated value is to be returned as String
     * @return the String value to which the specified name is mapped
     * @throws IllegalStateException    if the value is not assignable to JsonString type
     * @throws IllegalArgumentException if the value is not assignable to a UUID
     */
    public static Optional<UUID> getUUID(final JsonObject object, final String... names) {
        return getString(object, names)
                .map(string -> {
                    try {
                        return UUID.fromString(string);
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalStateException(String.format("Retrieved string '%s' is not a UUID", string), ex);
                    }
                });
    }

    /**
     * A convenience method for {@code JsonNumber.longValue}.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  whose associated value is to be returned as Long
     * @return the Long value to which the specified name is mapped
     * @throws IllegalStateException    if the value is not assignable to JsonNumber type
     * @throws IllegalArgumentException if the value is not assignable to a Long
     */
    public static Optional<Long> getLong(final JsonObject object, final String... names) {
        return getJsonValue(object, ValueType.NUMBER, JsonObject::getJsonNumber, names)
                .map(JsonNumber::longValue);
    }

    /**
     * A convenience method for getting a JsonArray as a List of a specific JsonValue type.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  whose associated value is to be returned
     * @param clazz  the type of JsonValue that the returned list will contain
     * @return the Long value to which the specified name is mapped
     * @throws IllegalStateException    if the value is not assignable to the specified type
     * @throws IllegalArgumentException if the value is not assignable to a Long
     */
    public static <R extends JsonValue> Optional<List<R>> getList(final JsonObject object, final Class<R> clazz, final String... names) {
        return getJsonValue(object, ValueType.ARRAY, JsonObject::getJsonArray, names)
                .map(jsonArray -> jsonArray.getValuesAs(clazz))
                .map(ImmutableList::copyOf);
    }

    /**
     * A convenience method for getting a list of a specific type.
     *
     * @param object    the JsonObject from which to retrieve the value
     * @param jsonClazz the JsonValue type that the value is stored as
     * @param converter a function that can convert from the JsonValue class to the required type
     * @param names     whose associated value is to be returned
     * @param <R>       the type of items in the return list
     * @param <J>       the JsonValue type that the value is stored as
     * @return an optional list of values found
     */
    public static <R, J extends JsonValue> Optional<List<R>> getList(final JsonObject object,
                                                                     final Class<J> jsonClazz,
                                                                     final Function<J, R> converter,
                                                                     final String... names) {
        return getList(object, jsonClazz, names)
                .map(list -> list.stream()
                        .map(converter)
                        .collect(Collectors.toList()))
                .map(ImmutableList::copyOf);
    }

    /**
     * Get a list of UUIDs from a JsonObject.
     *
     * @param object object the JsonObject from which to retrieve the list
     * @param names  the field name path whose associated value is to be returned
     * @return the list of UUIDs or an empty list if none were found
     */
    public static List<UUID> getUUIDs(final JsonObject object, final String... names) {
        return getList(object, JsonString.class, jsonString -> UUID.fromString(jsonString.getString()), names)
                .orElse(Collections.emptyList());
    }

    /**
     * Create a {@link JsonObjectBuilder} from an existing {@link JsonObject} applying the filter.
     * Only copy the field names for which the filter returns true.
     *
     * @param source {@link JsonObject} to copy fields from
     * @return a {@link JsonObjectBuilder} initialised with the fields contained in the source
     */
    public static JsonObjectBuilder createObjectBuilderWithFilter(final JsonObject source, Function<String, Boolean> filter) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        source.entrySet().stream().filter(e -> filter.apply(e.getKey())).forEach(x -> builder.add(x.getKey(), x.getValue()));
        return builder;
    }

    /**
     * Create a {@link JsonObjectBuilder} from an existing {@link JsonObject}.
     *
     * @param source {@link JsonObject} to copy fields from
     * @return a {@link JsonObjectBuilder} initialised with the fields contained in the source
     */
    public static JsonObjectBuilder createObjectBuilder(final JsonObject source) {
        return createObjectBuilderWithFilter(source, x -> true);
    }

    /**
     * Assert that the provided arguments are valid. The object must not be null, and the next field
     * name must be non-empty.
     *
     * @param object the JsonObject from which to retrieve the value
     * @param names  the field names
     */
    private static void checkArguments(final JsonObject object, final String... names) {
        if (object == null) {
            throw new IllegalArgumentException("Json object cannot be null");
        }
        if (names.length == 0) {
            throw new IllegalArgumentException("At least one level of field name must be provided");
        }
        if (names[0] == null || names[0].isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
    }
}
