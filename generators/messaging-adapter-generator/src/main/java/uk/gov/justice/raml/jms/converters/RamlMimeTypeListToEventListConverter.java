package uk.gov.justice.raml.jms.converters;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import uk.gov.justice.domain.subscriptiondescriptor.Event;

import java.util.Collection;
import java.util.List;

import org.raml.model.MimeType;

public class RamlMimeTypeListToEventListConverter {

    private final MimeTypeToEventConverter mimeTypeToEventConverter;

    public RamlMimeTypeListToEventListConverter(final MimeTypeToEventConverter mimeTypeToEventConverter) {
        this.mimeTypeToEventConverter = mimeTypeToEventConverter;
    }

    public List<Event> toEvents(final Collection<MimeType> mimeTypes) {

        if(shouldListenToAllMessages(mimeTypes)) {
            return emptyList();
        }

        return mimeTypes.stream()
                .map(mimeTypeToEventConverter::asEvent)
                .collect(toList());
    }

    private boolean shouldListenToAllMessages(final Collection<MimeType> mimeTypes) {

        return mimeTypes
                .stream()
                .anyMatch(mimeType -> APPLICATION_JSON.equals(mimeType.getType()));
    }
}
