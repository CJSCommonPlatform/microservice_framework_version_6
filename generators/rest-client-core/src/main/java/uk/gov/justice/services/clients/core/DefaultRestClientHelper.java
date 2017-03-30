package uk.gov.justice.services.clients.core;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper service for REST clients.
 */
public class DefaultRestClientHelper implements RestClientHelper {

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{(\\w+)\\}");

    /**
     * Extracts the path parameters from a path.
     *
     * @param path the path with parameter names wrapped in curly brackets
     * @return the set of parameter names found in the given path
     */
    @Override
    public Set<String> extractPathParametersFromPath(final String path) {

        return stream(path.split("/"))
                .filter(pathSegment -> PATH_PARAM_PATTERN.matcher(pathSegment).matches())
                .map(pathSegment -> getPathVariable(PATH_PARAM_PATTERN.matcher(pathSegment)))
                .collect(toSet());
    }

    private String getPathVariable(final Matcher matcher) {
        return matcher.find() ? matcher.group(0).substring(1, matcher.end() - 1) : null;
    }

}