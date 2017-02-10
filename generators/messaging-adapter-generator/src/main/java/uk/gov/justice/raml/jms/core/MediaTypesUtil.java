package uk.gov.justice.raml.jms.core;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.raml.model.ActionType.POST;

import java.util.Map;
import java.util.stream.Stream;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

class MediaTypesUtil {

    static Stream<MimeType> mediaTypesFrom(final Map<ActionType, Action> actions) {
        return actions.get(POST).getBody().values().stream();
    }

    static boolean containsGeneralJsonMimeType(final Map<ActionType, Action> actions) {
        return mediaTypesFrom(actions)
                .anyMatch(mimeType -> APPLICATION_JSON.equals(mimeType.getType()));
    }
}