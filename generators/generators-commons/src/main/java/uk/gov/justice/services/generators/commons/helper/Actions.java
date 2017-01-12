package uk.gov.justice.services.generators.commons.helper;


import static java.util.Collections.emptyList;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;

import java.util.Collection;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Response;

public final class Actions {

    private Actions() {
    }

    public static Collection<MimeType> responseMimeTypesOf(final Action action) {
        final Map<String, Response> responses = action.getResponses();

        if (hasResponse(responses)) {
            final Map<String, MimeType> bodyTypes = responses.values().iterator().next().getBody();

            if (hasResponse(bodyTypes)) {
                return bodyTypes.values();
            }
        }

        return emptyList();
    }

    public static boolean hasResponseMimeTypes(final Action action) {
        return hasResponse(action.getResponses());
    }

    public static boolean isSupportedActionType(final ActionType actionType) {
        return isSupportedActionTypeWithRequestType(actionType) || isSupportedActionTypeWithResponseTypeOnly(actionType);
    }

    public static boolean isSupportedActionTypeWithRequestType(final ActionType actionType) {
        return actionType == POST || actionType == PUT || actionType == PATCH || actionType == DELETE;
    }

    public static boolean isSupportedActionTypeWithResponseTypeOnly(final ActionType actionType) {
        return actionType == GET;
    }

    private static boolean hasResponse(final Map<String, ?> responses) {
        if (null != responses) {
            final Collection<?> values = responses.values();

            if (!values.isEmpty() && null != values.iterator().next()) {
                return true;
            }
        }
        return false;
    }
}
