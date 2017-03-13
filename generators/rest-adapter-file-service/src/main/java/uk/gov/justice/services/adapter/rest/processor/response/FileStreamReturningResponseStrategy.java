package uk.gov.justice.services.adapter.rest.processor.response;


import static java.lang.String.format;
import static javax.json.JsonValue.NULL;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.FILE_STREAM_RETURNING_RESPONSE_STRATEGY;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.fileservice.api.FileRetriever;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.domain.FileReference;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Named(FILE_STREAM_RETURNING_RESPONSE_STRATEGY)
public class FileStreamReturningResponseStrategy implements ResponseStrategy {

    private static final String FILE_ID_FIELD = "fileId";

    @Inject
    FileRetriever fileRetriever;

    @Inject
    ResponseStrategyHelper responseStrategyHelper;

    @Override
    public Response responseFor(final String action, final Optional<JsonEnvelope> result) {

        try {
            final FileReference fileReference = fileRetriever.retrieve(fileIdFrom(result)).orElseThrow(NotFoundException::new);
            return responseStrategyHelper.responseFor(action, result,
                    jsonEnvelope -> status(OK)
                            .entity(fileReference.getContentStream())
                            .build());
        } catch (FileServiceException e) {
            throw new InternalServerErrorException(e);
        }

    }

    private UUID fileIdFrom(final Optional<JsonEnvelope> result) {
        return getUUID(resultEnvelopeFrom(result).payloadAsJsonObject(), FILE_ID_FIELD)
                .orElseThrow(() -> new InternalServerErrorException(format("%s not set in result", FILE_ID_FIELD)));
    }

    private JsonEnvelope resultEnvelopeFrom(final Optional<JsonEnvelope> result) {
        final JsonEnvelope resultEnvelope = result.orElseThrow(()-> new InternalServerErrorException("Result should not be empty"));
        if (resultEnvelope.payload() == NULL) {
            throw new NotFoundException();
        }
        return resultEnvelope;
    }
}
