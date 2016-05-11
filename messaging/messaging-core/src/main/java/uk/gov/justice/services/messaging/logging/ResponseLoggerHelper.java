package uk.gov.justice.services.messaging.logging;

import uk.gov.justice.services.common.http.HeaderConstants;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static uk.gov.justice.services.common.http.HeaderConstants.*;

public final class ResponseLoggerHelper {

    private ResponseLoggerHelper() {}

    public static String toResponseTrace(final Response response) {

        final Optional<Integer> responseCode = Optional.ofNullable(Integer.valueOf(response.getStatus()));
        final Optional<String> mediaType = Optional.ofNullable(response.getMediaType().getType());
        final Optional<String> cppid = Optional.ofNullable(response.getHeaderString(ID));

        final JsonObjectBuilder builder = Json.createObjectBuilder();

        responseCode.ifPresent(integer -> builder.add("ResponseCode", integer));
        mediaType.ifPresent(s -> builder.add("MediaType", s));
        cppid.ifPresent(s -> builder.add(ID, s));

        return builder.build().toString();
    }
}
