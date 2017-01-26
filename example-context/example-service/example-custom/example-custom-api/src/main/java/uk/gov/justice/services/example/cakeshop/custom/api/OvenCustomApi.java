package uk.gov.justice.services.example.cakeshop.custom.api;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.custom.api.response.OvenStatus;
import uk.gov.justice.services.example.cakeshop.custom.api.response.OvensStatus;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@CustomServiceComponent("CUSTOM_API")
public class OvenCustomApi {

    @Inject
    private Enveloper enveloper;

    @Handles("example.ovens-status")
    public JsonEnvelope status(final JsonEnvelope query) {

        final OvensStatus status = new OvensStatus(asList(
                new OvenStatus(randomUUID(), "Big Oven", 250, true),
                new OvenStatus(randomUUID(), "Large Oven", 0, false)));

        return enveloper.withMetadataFrom(query, "example.ovens-status").apply(status);
    }
}
