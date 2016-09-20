package uk.gov.justice.services.test.utils.core.http;

import static javax.json.Json.createReader;

import uk.gov.justice.services.messaging.JsonObjects;

import java.io.StringReader;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * A predicate to test that the exact property name and property values are present in the response json.
 * The property name and values to check can be supplied to the predicate in a <code>Map</code> via constructor.
 *
 * To Use:
 * <pre><blockquote>
 *
 *      final Map<String, String> values = new HashMap<>();
 *
 *      values.put("name_1", "value_1");
 *      values.put("name_2", "value_2");
 *
 *      final ExpectedJsonValuesResultCondition expectedJsonValuesResultCondition =
 *      new ExpectedJsonValuesResultCondition(values);
 *
 * </blockquote></pre>
 */
public class ExpectedJsonValuesResultCondition implements Predicate<String> {

    private final Map<String, String> values;

    /**
     * Creates a predicate with <code>Map<String, String></code> property names and values to match in the json Response.
     * @param values <code>Map<String, String></code> the property name and value pairs to match in the json Response.
     */
    public ExpectedJsonValuesResultCondition(final Map<String, String> values) {
        this.values = values;
    }

    @Override
    public boolean test(final String entity) {
        if (entity != null) {
            final JsonReader jsonReader = createReader(new StringReader(entity));
            final JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            final boolean anyMatchFalse = values.entrySet().stream().map(entry -> {
                final Optional<String> value = JsonObjects.getString(jsonObject, entry.getKey());
                return value.isPresent() && value.get().equals(entry.getValue());
            }).anyMatch(e -> !e);

            return !anyMatchFalse;
        }

        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ExpectedJsonValuesResultCondition that = (ExpectedJsonValuesResultCondition) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "ExpectedJsonValuesResultCondition{" +
                "values=" + values +
                '}';
    }
}
