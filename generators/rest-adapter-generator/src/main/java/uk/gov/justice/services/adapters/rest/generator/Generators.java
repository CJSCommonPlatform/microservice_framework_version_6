package uk.gov.justice.services.adapters.rest.generator;


import static java.util.Comparator.comparing;
import static uk.gov.justice.services.generators.commons.helper.Names.baseUriPathWithoutContext;

import uk.gov.justice.services.core.annotation.Component;

import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.model.MimeType;
import org.raml.model.Raml;

final class Generators {
    private static final Pattern PILLAR_AND_TIER_PATTERN = Pattern
            .compile("(command/api|command/controller|command/handler|query/api|query/controller|query/view|event/api)");

    private Generators() {
    }

    /**
     * Derive the framework {@link Component} pillar and tier value from the base URI in the RAML.
     *
     * @param raml the RAML that provides the base URI
     * @return the {@link Component} value derived from the base URI
     */
    static Optional<Component> componentFromBaseUriIn(final Raml raml) {
        final Matcher matcher = PILLAR_AND_TIER_PATTERN.matcher(baseUriPathWithoutContext(raml));

        if (matcher.find()) {
            final String pillarAndTier = matcher.group(1);
            final String[] sections = pillarAndTier.split("/");
            return Optional.of(Component.valueOf(sections[0], sections[1]));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Comparator for ordering MimeType by the string representation returned by getType().
     *
     * @return the MimeType Comparator
     */
    static Comparator<MimeType> byMimeTypeOrder() {
        return comparing(MimeType::getType);
    }

}
