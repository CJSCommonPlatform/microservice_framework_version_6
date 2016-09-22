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
 * Handy Predicate which validates that the response body contains the properties and
 * values contained in the values Map.
 *
 * Will recurse the json dom matching against all name/value properties
 */
public class ExpectedJsonValuesResultCondition implements Predicate<String> {

    private final Map<String, String> values;

    public ExpectedJsonValuesResultCondition(final Map<String, String> values) {
        this.values = values;
    }

    @Override
    public boolean test(final String responseBody) {
        if (responseBody != null) {
            final JsonReader jsonReader = createReader(new StringReader(responseBody));
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
