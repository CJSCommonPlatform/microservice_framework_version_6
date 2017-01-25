package uk.gov.justice.services.generators.commons.helper;


import static java.lang.String.valueOf;
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

    private static final String ACCEPTED = valueOf(javax.ws.rs.core.Response.Status.ACCEPTED.getStatusCode());

    private Actions() {
    }

    /**
     * Returns a Collection of the Response {@link MimeType} that an {@link Action} supports,
     * otherwise an empty Collection.
     *
     * @param action the {@link Action} to check
     * @return the @{link Collection} of Response {@link MimeType}
     */
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

    /**
     * Synchronous {@link Action} will have {@link Response} types but will not have the
     * ACCEPTED(202) {@link Response} type.
     *
     * @param action the {@link Action} to check
     * @return true if action has responses but no ACCEPTED(202) type
     */
    public static boolean isSynchronousAction(final Action action) {
        final Map<String, Response> responses = action.getResponses();
        return hasResponse(responses) && !responses.containsKey(ACCEPTED);
    }

    /**
     * Returns true if the {@link ActionType} is a supported type.
     *
     * @param actionType the {@link ActionType} to check
     * @return true if supported type
     */
    public static boolean isSupportedActionType(final ActionType actionType) {
        return isSupportedActionTypeWithRequestType(actionType) || isSupportedActionTypeWithResponseTypeOnly(actionType);
    }

    /**
     * Returns true if the {@link ActionType} is a supported with Request type
     *
     * @param actionType the {@link ActionType} to check
     * @return true if supported type
     */
    public static boolean isSupportedActionTypeWithRequestType(final ActionType actionType) {
        return actionType == POST || actionType == PUT || actionType == PATCH || actionType == DELETE;
    }

    /**
     * Returns true if the {@link ActionType} is a supported Response only type
     *
     * @param actionType the {@link ActionType} to check
     * @return true if supported type
     */
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
