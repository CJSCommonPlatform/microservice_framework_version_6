package uk.gov.justice.services.adapters.rest.generator;


import static uk.gov.justice.services.generators.commons.helper.Names.baseUriPathWithoutContext;

import uk.gov.justice.services.core.annotation.Component;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.model.MimeType;
import org.raml.model.Raml;

final class Generators {
    private static final Pattern PILLAR_AND_TIER_PATTERN = Pattern
            .compile("(command/api|command/controller|command/handler|query/api|query/controller|query/view)");

    private Generators() {
    }

    /**
     * Derive the framework {@link Component} pillar and tier value from the base URI in the RAML.
     *
     * @param raml the RAML that provides the base URI
     * @return the {@link Component} value derived from the base URI
     */
    static Component componentFromBaseUriIn(final Raml raml) {
        final Matcher matcher = PILLAR_AND_TIER_PATTERN.matcher(baseUriPathWithoutContext(raml));

        if (matcher.find()) {
            final String pillarAndTier = matcher.group(1);
            final String[] sections = pillarAndTier.split("/");
            return Component.valueOf(sections[0], sections[1]);
        } else {
            throw new IllegalStateException(String.format("Base URI must contain valid pillar and tier: %s", raml.getBaseUri()));
        }
    }

    /**
     * Comparator for ordering MimeType by the string representation returned by getType().
     *
     * @return the MimeType Comparator
     */
    static Comparator<MimeType> byMimeTypeOrder() {
        return (t1, t2) -> t1.getType().compareTo(t2.getType());
    }

}
