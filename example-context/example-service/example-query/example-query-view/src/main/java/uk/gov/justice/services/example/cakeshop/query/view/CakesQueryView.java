package uk.gov.justice.services.example.cakeshop.query.view;

import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.service.CakeService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;


@ServiceComponent(QUERY_VIEW)
public class CakesQueryView {

    private final CakeService service;
    private final Enveloper enveloper;

    @Inject
    public CakesQueryView(final CakeService service, final Enveloper enveloper) {

        this.service = service;
        this.enveloper = enveloper;
    }

    @Handles("example.search-cakes")
    public JsonEnvelope cakes(final JsonEnvelope query) {
        return enveloper.withMetadataFrom(query, "example.search-cakes").apply(service.cakes());

    }
}
