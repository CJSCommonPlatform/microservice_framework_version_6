package uk.gov.justice.services.clients.core;

import static java.util.Arrays.stream;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper service for REST clients.
 */
public class RestClientHelper {

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{(\\w+)\\}");

    /**
     * Extracts the path parameters from a path.
     *
     * @param path the path with parameter names wrapped in curly brackets
     * @return the set of parameter names found in the given path
     */
    public Set<String> extractPathParametersFromPath(final String path) {

        return stream(path.split("/"))
                .filter(pathSegment -> PATH_PARAM_PATTERN.matcher(pathSegment).matches())
                .map(pathSegment -> getPathVariable(PATH_PARAM_PATTERN.matcher(pathSegment)))
                .collect(Collectors.toSet());
    }

    private String getPathVariable(final Matcher matcher) {
        return matcher.find() ? matcher.group(0).substring(1, matcher.end() - 1) : null;
    }

}
