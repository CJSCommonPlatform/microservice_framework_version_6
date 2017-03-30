package uk.gov.justice.services.clients.core;

import java.util.Set;

public interface RestClientHelper {

    /**
     * Extracts the path parameters from a path.
     *
     * @param path the path with parameter names wrapped in curly brackets
     * @return the set of parameter names found in the given path
     */
    Set<String> extractPathParametersFromPath(final String path);
}