package uk.gov.justice.raml.maven.lintchecker.rules.utils;

import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.listOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;

public final class RamlActionFinder {

    private RamlActionFinder() {
    }

    public static Collection<String> actionsFrom(final Raml raml) {

        final Collection<Resource> resources = raml.getResources().values();
        final List<String> actionNames = new ArrayList<>();

        resources.forEach(resource -> resource.getActions().values()
                .forEach(action -> listOf(action.getDescription())
                        .forEach(actionMapping -> actionNames.add(actionMapping.getName()))));
        return actionNames;
    }
}