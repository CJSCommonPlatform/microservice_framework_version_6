package uk.gov.justice.services.adapters.rest.generator;

import org.apache.commons.lang.StringUtils;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang.WordUtils.capitalize;

public class Names {
    public static final Set<String> JAVA_KEYWORDS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                    "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final",
                    "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
                    "interface", "long", "native", "new", "null", "package", "private", "protected", "public",
                    "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw",
                    "throws", "transient", "true", "try", "void", "volatile", "while")));

    public static final String GENERIC_PAYLOAD_ARGUMENT_NAME = "entity";
    public static final String MULTIPLE_RESPONSE_HEADERS_ARGUMENT_NAME = "headers";
    public static final String EXAMPLE_PREFIX = " e.g. ";
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String INTERFACE_NAME_SUFFIX = "Resource";

    private Names() {
    }

    public static String resourceInterfaceNameOf(final Resource resource) {
        final String resourceInterfaceName = buildJavaFriendlyName(defaultIfBlank(resource.getDisplayName(),
                resource.getRelativeUri()));

        return isBlank(resourceInterfaceName) ? "Root" : resourceInterfaceName.concat(INTERFACE_NAME_SUFFIX);
    }

    public static String buildVariableName(final String source) {
        final String name = uncapitalize(buildJavaFriendlyName(source));

        return JAVA_KEYWORDS.contains(name) ? "$" + name : name;
    }

    public static String buildJavaFriendlyName(final String source) {
        final String baseName = source.replaceAll("[\\W_]", " ");
        return capitalize(baseName).replaceAll("[\\W_]", "");
    }

    public static String buildResourceMethodName(final Action action, final MimeType bodyMimeType) {
        final String methodBaseName = buildJavaFriendlyName(action.getResource()
                .getUri()
                .replace("{", " By "));

        return action.getType().toString().toLowerCase() + buildMimeTypeInfix(bodyMimeType) + methodBaseName;
    }

    public static String buildMimeTypeInfix(final MimeType bodyMimeType) {
        return bodyMimeType != null ? buildJavaFriendlyName(getShortMimeType(bodyMimeType)) : "";
    }

    public static String getShortMimeType(final MimeType mimeType) {
        if (mimeType == null) {
            return "";
        }
        String subType = substringAfter(mimeType.getType()
                .toLowerCase(DEFAULT_LOCALE), "/");

        if (subType.contains(".")) {
            // handle types like application/vnd.example.v1+json
            StringBuilder sb = new StringBuilder();
            for (String s : subType.split("\\W+")) {
                sb.append(sb.length() == 0 ? s : StringUtils.capitalize(s));
            }
            return sb.toString();
        } else {
            // handle any other types
            return remove(remove(remove(subType, "x-www-"), "+"), "-");
        }

    }

}
