package uk.gov.justice.services.core.json;

import static java.lang.String.join;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

public class DefaultJsonValidationLoggerHelper implements JsonValidationLoggerHelper {

    public String toValidationTrace(final JsonSchemaValidationException jsonSchemaValidatoinException) {
        return toJsonObject(jsonSchemaValidatoinException).toString();
    }

    public JsonObject toJsonObject(final JsonSchemaValidationException jsonSchemaValidationException) {
        return buildResponse((ValidationException) jsonSchemaValidationException.getCause());
    }

    public String toValidationTrace(final ValidationException validationException) {
        return toJsonObject(validationException).toString();
    }

    public JsonObject toJsonObject(final ValidationException validationException) {
        return buildResponse(validationException);
    }

    private JsonObject buildResponse(final ValidationException validationException) {

        final JsonObjectBuilder builder = createObjectBuilder();

        Optional.ofNullable(validationException.getMessage())
                .ifPresent(message -> builder.add("message", message));
        Optional.ofNullable(validationException.getViolatedSchema())
                .ifPresent(schema -> builder.add("violatedSchema", getSchemaName(schema)));
        Optional.ofNullable(validationException.getPointerToViolation())
                .ifPresent(violation -> builder.add("violation", violation));

        final JsonArrayBuilder arrayBuilder = createArrayBuilder();
        validationException.getCausingExceptions()
                .forEach(exception -> arrayBuilder.add(buildResponse(exception)));
        builder.add("causingExceptions", arrayBuilder.build());

        return builder.build();
    }

    private String getSchemaName(final Schema schema) {

        final List<String> elements = new ArrayList<>();

        if (schema.getTitle() != null) {
            elements.add(schema.getTitle());
        }

        if (schema.getId() != null) {
            elements.add(schema.getId());
        }

        if (schema.getDescription() != null) {
            elements.add(schema.getDescription());
        }

        return join(" - ", elements);
    }


}