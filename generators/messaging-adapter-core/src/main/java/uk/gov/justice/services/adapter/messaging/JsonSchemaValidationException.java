package uk.gov.justice.services.adapter.messaging;

public class JsonSchemaValidationException extends RuntimeException {
    public JsonSchemaValidationException(String s, Exception e) {
        super(s, e);
    }
}
