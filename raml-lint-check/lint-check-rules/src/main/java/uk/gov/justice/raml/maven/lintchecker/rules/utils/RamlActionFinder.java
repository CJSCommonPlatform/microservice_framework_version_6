package uk.gov.justice.raml.maven.lintchecker.rules.utils;

import uk.gov.justice.services.generators.commons.mapping.ActionMappingParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.raml.model.Raml;
import org.raml.model.Resource;

public final class RamlActionFinder {

    private RamlActionFinder() {
    }

    public static Collection<String> actionsFrom(final Raml raml) {

        final Collection<Resource> resources = raml.getResources().values();
        final List<String> actionNames = new ArrayList<>();

        resources.forEach(resource -> resource.getActions().values()
                .forEach(action -> new ActionMappingParser().listOf(action.getDescription())
                        .forEach(actionMapping -> actionNames.add(actionMapping.getName()))));
        return actionNames;
    }
}
