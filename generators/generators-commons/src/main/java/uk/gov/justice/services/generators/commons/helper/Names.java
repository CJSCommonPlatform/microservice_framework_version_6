package uk.gov.justice.services.generators.commons.helper;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.WordUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.listOf;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.generators.commons.mapping.ActionMapping;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

public final class Names {

    public static final String DEFAULT_ANNOTATION_PARAMETER = "value";
    public static final String GENERIC_PAYLOAD_ARGUMENT_NAME = "entity";
    public static final String RESOURCE_PACKAGE_NAME = "resource";
    public static final String MAPPER_PACKAGE_NAME = "mapper";
    public static final String JAVA_FILENAME_SUFFIX = ".java";

    private static final Set<String> JAVA_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                    "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final",
                    "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
                    "interface", "long", "native", "new", "null", "package", "private", "protected", "public",
                    "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw",
                    "throws", "transient", "true", "try", "void", "volatile", "while")));
    private static final String INTERFACE_NAME_SUFFIX = "Resource";
    private static final String APPLICATION_NAME_SUFFIX = "Application";
    private static final String ACTION_MAPPER_CLASS_SUFFIX = "ActionMapper";
    private static final String BLANK = "";

    private Names() {
    }

    public static String applicationNameFrom(final Raml raml) {
        return buildJavaFriendlyName(baseUriPathWithoutContext(raml))
                .concat(APPLICATION_NAME_SUFFIX);
    }

    public static String baseUriPathWithoutContext(final Raml raml) {
        try {
            final URL url = new URL(raml.getBaseUri());
            final String path = url.getPath();
            if (path.indexOf('/', 1) == -1) {
                return path;
            }
            return path.substring(path.indexOf('/', 1));
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Base URI must be a valid URL", ex);
        }
    }

    public static String resourceInterfaceNameOf(final Resource resource) {
        final String resourceInterfaceName = buildJavaFriendlyName(defaultIfBlank(resource.getDisplayName(),
                resource.getRelativeUri()));

        return isBlank(resourceInterfaceName) ? "Root" : resourceInterfaceName.concat(INTERFACE_NAME_SUFFIX);
    }

    public static String resourceImplementationNameOf(final Resource resource) {
        return format("Default%s", resourceInterfaceNameOf(resource));
    }

    public static String buildResourceMethodNameWithNoMimeType(final Action action) {
        return buildResourceMethodNameWith(action, () -> BLANK);
    }

    public static String buildResourceMethodName(final Action action, final MimeType bodyMimeType) {
        if (null == bodyMimeType) {
            return buildResourceMethodNameWithNoMimeType(action);
        }

        return buildResourceMethodNameWith(action, () -> {
            if (!isEmpty(action.getDescription())) {
                final Optional<ActionMapping> mapping = listOf(action.getDescription()).stream()
                        .filter(actionMapping -> filterActionMapping(actionMapping, bodyMimeType.getType()))
                        .findFirst();

                if (mapping.isPresent()) {
                    return buildJavaFriendlyName(camelCase(mapping.get().getName()));
                }
            }

            return BLANK;
        });
    }

    public static String packageNameOf(final GeneratorConfig configuration, final String subPackageName) {
        final StringBuilder packageBuilder = new StringBuilder().append(configuration.getBasePackageName());
        if (isNotEmpty(subPackageName)) {
            packageBuilder.append('.').append(subPackageName);
        }
        return packageBuilder.toString();
    }

    /**
     * Removes the application/vnd and anything after and including '+' and returns as the name.
     *
     * @param mimeType the mime type to parse
     * @return String representing the name from the mime type
     */
    public static String nameFrom(final MimeType mimeType) {
        final String mimeType1 = mimeType.toString();
        final String section = mimeType1.substring(mimeType1.indexOf('.') + 1);
        return section.substring(0, section.indexOf('+'));
    }


    /**
     * Construct delimiter separated list of command/event names
     *
     * @param mediaTypes mediaTypes to create the list from
     * @param delimiter  the delimiter to separates names with
     * @return delimiter separated list of command/event names
     */
    public static String namesListStringFrom(final Stream<MimeType> mediaTypes, final String delimiter) {
        return mediaTypes.map(Names::nameFrom)
                .collect(joining(delimiter));
    }

    public static String mapperClassNameOf(final Resource resource) {
        return resourceImplementationNameOf(resource) + ACTION_MAPPER_CLASS_SUFFIX;
    }

    public static String camelCase(final String subType) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : subType.split("\\W+")) {
            sb.append(sb.length() == 0 ? s : StringUtils.capitalize(s));
        }
        return sb.toString();
    }

    private static boolean filterActionMapping(final ActionMapping actionMapping, final String bodyMimeType) {
        return bodyMimeType.equals(actionMapping.getRequestType()) || bodyMimeType.equals(actionMapping.getResponseType());
    }

    private static String buildResourceMethodNameWith(final Action action, final Supplier<String> rootMethodName) {
        final String actionType = action.getType().toString().toLowerCase();
        return actionType
                + rootMethodName.get()
                + buildJavaFriendlyName(action.getResource().getUri().replace("{", " By "));
    }

    private static String buildJavaFriendlyName(final String source) {
        final String baseName = source.replaceAll("[\\W_]", " ");
        return capitalize(baseName).replaceAll("[\\W_]", "");
    }
}
