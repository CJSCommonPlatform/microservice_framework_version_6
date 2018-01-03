package uk.gov.justice.services.generators.commons.mapping;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.core.mapping.MediaType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;

public class ActionNameToMediaTypesParser {

    private final ActionMappingParser actionMappingParser;

    public ActionNameToMediaTypesParser(final ActionMappingParser actionMappingParser) {
        this.actionMappingParser = actionMappingParser;
    }

    /**
     * Parse the action name to media types from the given RAML.
     *
     * @param raml the RAML to parse
     * @return List of {@link ActionNameMapping}
     */
    public List<ActionNameMapping> parseFrom(final Raml raml) {
        final Collection<Resource> resources = raml.getResources().values();

        return resources.stream()
                .flatMap(this::eachResource)
                .collect(toList());
    }

    /**
     * For a given resource process all Actions
     *
     * @param resource the Resource to process
     * @return Stream of {@link ActionNameMapping} or empty Stream if getActions returns null
     */
    private Stream<ActionNameMapping> eachResource(final Resource resource) {
        return ofNullable(resource.getActions())
                .map(this::processActions)
                .orElseGet(Stream::empty);
    }

    /**
     * Process the Actions of a Resource
     *
     * @param actionTypeActionMap the Map of Actions
     * @return Stream of {@link ActionNameMapping}
     */
    private Stream<ActionNameMapping> processActions(final Map<ActionType, Action> actionTypeActionMap) {
        return actionTypeActionMap.values().stream()
                .flatMap(this::eachAction);
    }

    /**
     * Process Action if not null otherwise return Stream.empty()
     *
     * @param action the Action to process
     * @return Stream of {@link ActionNameMapping} or empty Stream if Action is null
     */
    private Stream<ActionNameMapping> eachAction(final Action action) {
        return ofNullable(action)
                .map(this::processAction)
                .orElseGet(Stream::empty);
    }

    /**
     * Process an Action's body (MimeTypes)
     *
     * @param action the Action to process
     * @return Stream of {@link ActionNameMapping}
     */
    private Stream<ActionNameMapping> processAction(final Action action) {
        final List<ActionMapping> actionMappings = actionMappingParser.listOf(action.getDescription());

        return actionMappings.stream()
                .map(actionMapping -> {
                    final Optional<String> requestType = Optional.ofNullable(actionMapping.getRequestType());
                    final Optional<String> responseType = Optional.ofNullable(actionMapping.getResponseType());

                    return new ActionNameMapping(
                            actionMapping.getName(),
                            requestType.map(MediaType::new).orElse(null),
                            responseType.map(MediaType::new).orElse(null));
                });
    }
}
