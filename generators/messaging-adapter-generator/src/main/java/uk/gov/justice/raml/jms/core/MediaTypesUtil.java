package uk.gov.justice.raml.jms.core;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;
import java.util.stream.Stream;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

class MediaTypesUtil {
    static Stream<MimeType> mediaTypesFrom(final Map<ActionType, Action> actions) {
        return actions.get(ActionType.POST).getBody().values().stream();
    }

    static boolean containsGeneralJsonMimeType(final Map<ActionType, Action> actions) {
        return mediaTypesFrom(actions)
                .filter(mimeType -> mimeType.getType().equals(APPLICATION_JSON))
                .findAny()
                .isPresent();
    }
}
