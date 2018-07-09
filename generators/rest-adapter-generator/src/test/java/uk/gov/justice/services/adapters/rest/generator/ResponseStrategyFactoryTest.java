package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.valueOf;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.ACCEPTED_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.ACCEPTED_STATUS_NO_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.FILE_STREAM_RETURNING_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.OK_STATUS_ENVELOPE_PAYLOAD_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.ResponseBuilder.response;

import java.util.Map;
import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.raml.model.Action;
import org.raml.model.Response;

public class ResponseStrategyFactoryTest {

    private ResponseStrategyFactory responseStrategyFactory = new ResponseStrategyFactory();

    @Test
    public void shouldReturnFileStreamReturningResponseStrategyNameIfResponseTypeIsOctetStream() {
        final Action action = httpAction()
                .withResponseTypes(APPLICATION_OCTET_STREAM)
                .build();

        final String strategyName = responseStrategyFactory.nameOfResponseStrategyFor(action, empty());

        assertThat(strategyName, CoreMatchers.is(FILE_STREAM_RETURNING_RESPONSE_STRATEGY));
    }

    @Test
    public void shouldReturnAcceptedStatusNoEntityResponseStrategyNameIfResponseIsAccepted() {
        final Map<String, Response> responses = singletonMap(
                valueOf(ACCEPTED.getStatusCode()), response().build());

        final Action action = httpAction()
                .withResponsesFrom(responses)
                .build();

        final String strategyName = responseStrategyFactory.nameOfResponseStrategyFor(action, empty());

        assertThat(strategyName, CoreMatchers.is(ACCEPTED_STATUS_NO_ENTITY_RESPONSE_STRATEGY));
    }

    @Test
    public void shouldReturnAcceptedStatusEnvelopeEntityResponseStrategyNameIfResponseIsAcceptedAndHasResponseType() {
        final Map<String, Response> responses = singletonMap(
                valueOf(ACCEPTED.getStatusCode()), response().withBodyTypes("application/json").build());

        final Action action = httpAction()
                .withResponsesFrom(responses)
                .build();

        final String strategyName = responseStrategyFactory.nameOfResponseStrategyFor(action, empty());

        assertThat(strategyName, CoreMatchers.is(ACCEPTED_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY));
    }

    @Test
    public void shouldReturnOkStatusEnvelopeEntityResponseStrategyNameForQueryControllerComponent() {
        final Action action = httpAction()
                .withResponseTypes("application/json")
                .build();
        final Optional<String> component = Optional.of(QUERY_CONTROLLER);

        final String strategyName = responseStrategyFactory.nameOfResponseStrategyFor(action, component);

        assertThat(strategyName, CoreMatchers.is(OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY));
    }

    @Test
    public void shouldReturnOkStatusEnvelopeEntityResponseStrategyNameForQueryViewComponent() {
        final Action action = httpAction()
                .withResponseTypes("application/json")
                .build();
        final Optional<String> component = Optional.of(QUERY_VIEW);

        final String strategyName = responseStrategyFactory.nameOfResponseStrategyFor(action, component);

        assertThat(strategyName, CoreMatchers.is(OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY));
    }

    @Test
    public void shouldReturnOkStatusEnvelopePayloadEntityResponseStrategyNameForEverythingElse() {
        final Action action = httpAction()
                .withResponseTypes("application/json")
                .build();

        final String strategyName = responseStrategyFactory.nameOfResponseStrategyFor(action, empty());

        assertThat(strategyName, CoreMatchers.is(OK_STATUS_ENVELOPE_PAYLOAD_ENTITY_RESPONSE_STRATEGY));
    }
}