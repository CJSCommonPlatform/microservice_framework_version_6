package uk.gov.justice.services.adapter.rest.envelope;

import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

/**
 * Wrapper for {@link MediaType} that can extract logical names from the subtype.
 */
public class StructuredMediaType {

    private final MediaType mediaType;

    /**
     * Constructor.
     *
     * @param mediaType the media type
     */
    public StructuredMediaType(final MediaType mediaType) {
        this.mediaType = mediaType;
    }

    private static Collector<String, ?, String> singletonCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException(String.format("Expected one and only one subtype beginning with vnd; found %d.", list.size()));
                    }
                    return list.get(0);
                }
        );
    }

    /**
     * Get the logical name from the media subtype.
     *
     * @return the logical name
     */
    public String getName() {
        return Pattern.compile("\\+")
                .splitAsStream(mediaType.getSubtype())
                .filter(s -> s.startsWith("vnd."))
                .map(s -> s.replaceFirst("^vnd\\.", ""))
                .collect(singletonCollector());
    }
}
