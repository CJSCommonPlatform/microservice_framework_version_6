package uk.gov.justice.services.adapters.rest.generator;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang.WordUtils.capitalize;

import uk.gov.justice.services.core.annotation.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

final class Names {

    private static final Set<String> JAVA_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                    "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final",
                    "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
                    "interface", "long", "native", "new", "null", "package", "private", "protected", "public",
                    "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw",
                    "throws", "transient", "true", "try", "void", "volatile", "while")));

    static final String GENERIC_PAYLOAD_ARGUMENT_NAME = "entity";
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String INTERFACE_NAME_SUFFIX = "Resource";
    private static final String APPLICATION_NAME_SUFFIX = "Application";

    private Names() {
    }

    static String applicationNameOf(final Raml raml) {
        return buildJavaFriendlyName(baseUriPathWithoutContext(raml))
                .concat(APPLICATION_NAME_SUFFIX);
    }

    static String baseUriPathWithoutContext(final Raml raml) {
        try {
            final URL url = new URL(raml.getBaseUri());
            final String path = url.getPath();
            if (path.indexOf("/", 1) == -1) {
                return path;
            }
            return path.substring(path.indexOf("/", 1));
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Base URI must be a valid URL", ex);
        }
    }

    static String resourceInterfaceNameOf(final Resource resource) {
        final String resourceInterfaceName = buildJavaFriendlyName(defaultIfBlank(resource.getDisplayName(),
                resource.getRelativeUri()));

        return isBlank(resourceInterfaceName) ? "Root" : resourceInterfaceName.concat(INTERFACE_NAME_SUFFIX);
    }

    static String buildVariableName(final String source) {
        final String name = uncapitalize(buildJavaFriendlyName(source));

        return JAVA_KEYWORDS.contains(name) ? "$" + name : name;
    }

    private static String buildJavaFriendlyName(final String source) {
        final String baseName = source.replaceAll("[\\W_]", " ");
        return capitalize(baseName).replaceAll("[\\W_]", "");
    }

    static String buildResourceMethodName(final Action action, final MimeType bodyMimeType) {
        final String methodBaseName = buildJavaFriendlyName(action.getResource()
                .getUri()
                .replace("{", " By "));

        return action.getType().toString().toLowerCase() + buildMimeTypeInfix(bodyMimeType) + methodBaseName;
    }

    static String buildMimeTypeInfix(final MimeType bodyMimeType) {
        return bodyMimeType != null ? buildJavaFriendlyName(getShortMimeType(bodyMimeType)) : "";
    }

    static String getShortMimeType(final MimeType mimeType) {
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

    static Component componentFromBaseUriIn(final Raml raml) {
        Map<String, String> conversion = ImmutableMap.of("command", "commands", "event", "events", "query", "queries");

        String baseUri = baseUriPathWithoutContext(raml);
        String[] sections = baseUri.split("/");

        return Component.valueOf(conversion.get(sections[1]), sections[2]);
    }
}
