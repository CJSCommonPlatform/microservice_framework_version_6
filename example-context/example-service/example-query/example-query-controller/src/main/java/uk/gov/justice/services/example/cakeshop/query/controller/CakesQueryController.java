package uk.gov.justice.services.example.cakeshop.query.controller;

import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(QUERY_CONTROLLER)
public class CakesQueryController {

    @Inject
    Requester requester;

    @Handles("example.search-cakes")
    public JsonEnvelope cakes(final JsonEnvelope query) {
        return requester.request(query);
    }

}
