package uk.gov.justice.raml.jms.converters;

import static java.lang.String.format;

import uk.gov.justice.services.generators.commons.mapping.SchemaIdParser;
import uk.gov.justice.services.generators.commons.mapping.SchemaParsingException;
import uk.gov.justice.subscription.domain.Event;

import org.raml.model.MimeType;

public class MimeTypeToEventConverter {

    private final EventNameExtractor eventNameExtractor;
    private final SchemaIdParser schemaIdParser;

    public MimeTypeToEventConverter(final EventNameExtractor eventNameExtractor, final SchemaIdParser schemaIdParser) {
        this.eventNameExtractor = eventNameExtractor;
        this.schemaIdParser = schemaIdParser;
    }

    public Event asEvent(final MimeType mimeType) {

        final String eventName = eventNameExtractor.extractEventName(mimeType.getType());
        final String schemaUri = schemaIdParser.schemaIdFrom(mimeType).orElseThrow(() -> new SchemaParsingException(
                format("Schema for media type: %s has no schema id",
                        mimeType.getType())));

        return new Event(eventName, schemaUri);
    }
}
