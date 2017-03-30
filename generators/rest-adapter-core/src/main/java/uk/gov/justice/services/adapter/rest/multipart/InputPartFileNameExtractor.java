package uk.gov.justice.services.adapter.rest.multipart;

import static java.util.regex.Pattern.compile;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;

@ApplicationScoped
public class InputPartFileNameExtractor {

    private static final String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";
    private static final Pattern FIND_FILENAME_PATTERN = compile("^.*filename=\"(.*)\".*$");
    private static final int FILENAME_MATCHER_GROUP = 1;

    public String extractFileName(final InputPart filePart) {

        final String headerValue = filePart
                .getHeaders()
                .getFirst(CONTENT_DISPOSITION_HEADER_NAME);

        if (headerValue == null) {
            throw new BadRequestException("No header found named '" + CONTENT_DISPOSITION_HEADER_NAME + "'");
        }

        final Matcher matcher = FIND_FILENAME_PATTERN.matcher(headerValue);

        if (matcher.find()) {
            return matcher.group(FILENAME_MATCHER_GROUP);
        }

        throw new BadRequestException("Failed to find 'filename' in '" + CONTENT_DISPOSITION_HEADER_NAME + "' header");
    }
}
