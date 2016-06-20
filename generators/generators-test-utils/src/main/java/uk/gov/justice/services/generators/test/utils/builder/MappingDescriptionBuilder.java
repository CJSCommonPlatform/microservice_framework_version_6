package uk.gov.justice.services.generators.test.utils.builder;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.LinkedList;
import java.util.List;

public class MappingDescriptionBuilder {

    private static final String MAPPINGS_BOUNDARY = "...\n";

    private List<MappingBuilder> mappings = new LinkedList<>();

    public static MappingDescriptionBuilder mappingDescription() {
        return new MappingDescriptionBuilder();
    }

    public static MappingDescriptionBuilder mappingDescriptionWith(final MappingBuilder... mappingBuilders) {
        return new MappingDescriptionBuilder().with(mappingBuilders);
    }

    public MappingDescriptionBuilder with(final MappingBuilder... mappingBuilders) {
        mappings.addAll(asList(mappingBuilders));
        return this;
    }

    public String build() {
        return format("%s%s%s",
                MAPPINGS_BOUNDARY,
                mappings.stream().map(MappingBuilder::build).collect(joining()),
                MAPPINGS_BOUNDARY);
    }

}
