package uk.gov.justice.services.generators.commons.mapping;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionType;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionTypeWithResponseTypeOnly;
import static uk.gov.justice.services.generators.commons.helper.Actions.responseMimeTypesOf;

import uk.gov.justice.services.core.mapping.MediaType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

/**
 * Parses the media types and the schema ids from the RAML.
 */
public class MediaTypeToSchemaIdParser {

    private final SchemaIdParser schemaIdParser;

    public MediaTypeToSchemaIdParser(final SchemaIdParser schemaIdParser) {
        this.schemaIdParser = schemaIdParser;
    }

    /**
     * Parse the media types to schema ids from the given RAML.
     *
     * @param raml the RAML to parse
     * @return List of {@link MediaTypeToSchemaId}
     */
    public List<MediaTypeToSchemaId> parseFrom(final Raml raml) {
        final Collection<Resource> resources = raml.getResources().values();

        return resources.stream()
                .flatMap(this::eachResource)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    /**
     * For a given resource process all Actions
     *
     * @param resource the Resource to process
     * @return Stream of Optional {@link MediaTypeToSchemaId} or empty Stream if getActions returns null
     */
    private Stream<Optional<MediaTypeToSchemaId>> eachResource(final Resource resource) {
        return ofNullable(resource.getActions())
                .map(this::processActions)
                .orElseGet(Stream::empty);
    }

    /**
     * Process the Actions of a Resource
     *
     * @param actionTypeActionMap the Map of Actions
     * @return Stream of Optional {@link MediaTypeToSchemaId}
     */
    private Stream<Optional<MediaTypeToSchemaId>> processActions(final Map<ActionType, Action> actionTypeActionMap) {
        return actionTypeActionMap.values().stream()
                .flatMap(this::eachAction);
    }

    /**
     * Process Action if not null otherwise return Stream.empty()
     *
     * @param action the Action to process
     * @return Stream of Optional {@link MediaTypeToSchemaId} or empty Stream if Action is null
     */
    private Stream<Optional<MediaTypeToSchemaId>> eachAction(final Action action) {
        return ofNullable(action)
                .map(this::processAction)
                .orElseGet(Stream::empty);
    }

    /**
     * Process an Action's body (MimeTypes)
     *
     * @param action the Action to process
     * @return Stream of Optional {@link MediaTypeToSchemaId} or empty Stream if getBody() returns null
     */
    private Stream<Optional<MediaTypeToSchemaId>> processAction(final Action action) {
        final ActionType actionType = action.getType();

        if (isSupportedActionType(actionType)) {
            if (isSupportedActionTypeWithResponseTypeOnly(actionType)) {
                return processMimeTypes(responseMimeTypesOf(action));
            } else {
                return processMimeTypes(action.getBody().values());
            }
        }

        return Stream.empty();
    }

    /**
     * Process the MimeTypes
     *
     * @param mimeTypes the Map of MimeTypes
     * @return Stream of Optional {@link MediaTypeToSchemaId}
     */
    private Stream<Optional<MediaTypeToSchemaId>> processMimeTypes(final Collection<MimeType> mimeTypes) {
        return mimeTypes.stream()
                .map(this::createMediaTypeToSchemaId);
    }

    /**
     * Create a {@link MediaTypeToSchemaId} object for the given MimeType. Use the
     * {@link SchemaIdParser} to get the schema id from the MimeType.
     *
     * @param mimeType the MimeType
     * @return Optional {@link MediaTypeToSchemaId}
     */
    private Optional<MediaTypeToSchemaId> createMediaTypeToSchemaId(final MimeType mimeType) {
        return schemaIdParser.schemaIdFrom(mimeType)
                .map(id -> new MediaTypeToSchemaId(new MediaType(mimeType.getType()), id));
    }
}
