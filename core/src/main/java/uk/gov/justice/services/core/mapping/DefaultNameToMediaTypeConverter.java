package uk.gov.justice.services.core.mapping;

import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert from {@link MediaType} to Action Name and from Action Name to {@link MediaType}
 */
public class DefaultNameToMediaTypeConverter implements NameToMediaTypeConverter {

    private static final String TYPE = "application";
    private static final String SUBTYPE_PREFIX = "vnd.";
    private static final String SUBTYPE_SUFFIX = "+json";

    private static final String REGEX = "^vnd\\.(.*)\\+json$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * Convert a given Action Name to a {@link MediaType}
     *
     * @param name the Action Name to convert
     * @return {@link MediaType}
     */
    public MediaType convert(final String name) {
        return new MediaType(TYPE, SUBTYPE_PREFIX + name + SUBTYPE_SUFFIX);
    }

    public String convert(final MediaType mediaType) {
        final String subtype = mediaType.getSubtype();

        final Matcher matcher = PATTERN.matcher(subtype);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new MalformedMediaTypeNameException(format("Failed to extract Name from media type '%s'", mediaType.toString()));
    }
}
