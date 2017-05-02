package uk.gov.justice.services.generators.commons.helper;

import static java.util.Optional.empty;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;

import uk.gov.justice.services.core.annotation.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestResourceBaseUri {

    private static final Pattern PILLAR_AND_TIER_PATTERN = Pattern
            .compile("(command/api|command/controller|command/handler|query/api|query/controller|query/view|event/api)");

    private final String pathWithoutWebContext;
    private Optional<String> component;

    public RestResourceBaseUri(final String baseUriString) {
        this.pathWithoutWebContext = pathWithoutContextFrom(baseUriString);

    }

    /**
     * @return base uri path with removed web context
     */
    public String pathWithoutWebContext() {
        return pathWithoutWebContext;
    }

    /**
     * Derive the framework component name pillar and tier value from the base URI.
     *
     * @return the component name derived from the base URI
     */
    public Optional<String> component() {
        if (component == null) {
            component = componentFrom(pathWithoutWebContext);
        }
        return component;
    }

    public String classNamePrefix() {
        return buildJavaFriendlyName(component().isPresent() ? component().get().toLowerCase() : pathWithoutWebContext());
    }

    private String pathWithoutContextFrom(final String baseUriString) {
        try {
            final String path = new URL(baseUriString).getPath();
            if (path.indexOf('/', 1) == -1) {
                return path;
            }
            return path.substring(path.indexOf('/', 1));
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Base URI must be a valid URL", ex);
        }
    }

    private Optional<String> componentFrom(final String pathWithoutWebContext) {
        final Matcher matcher = PILLAR_AND_TIER_PATTERN.matcher(pathWithoutWebContext);

        if (matcher.find()) {
            final String pillarAndTier = matcher.group(1);
            final String[] sections = pillarAndTier.split("/");
            return Optional.of(Component.valueOf(sections[0], sections[1]));
        } else {
            return empty();
        }
    }
}
