package uk.gov.justice.raml.jms.core;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.justice.raml.jms.uri.ResourceUri;

import java.util.List;

class TemplateAttributes {
    final List<Pair<String, String>> mainAttributes;
    final List<Pair<String, String>> activationConfigAttributes;
    final ResourceUri resourceUri;

    TemplateAttributes(final List<Pair<String, String>> attributesMap,
                       final List<Pair<String, String>> activationConfigAttributes,
                       final ResourceUri resourceUri) {
        this.mainAttributes = attributesMap;
        this.activationConfigAttributes = activationConfigAttributes;
        this.resourceUri = resourceUri;
    }
}