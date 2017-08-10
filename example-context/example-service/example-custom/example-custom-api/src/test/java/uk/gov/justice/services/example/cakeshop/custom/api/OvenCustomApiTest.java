package uk.gov.justice.services.example.cakeshop.custom.api;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isCustomHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OvenCustomApiTest {

    @Spy
    private Enveloper enveloper = createEnveloper();

    @InjectMocks
    private OvenCustomApi ovenCustomApi;

    @Test
    public void shouldHandleOvenStatus() throws Exception {
        assertThat(OvenCustomApi.class, isCustomHandlerClass("CUSTOM_API")
                .with(method("status").thatHandles("example.ovens-status")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnStatusOfAllOvens() throws Exception {
        final JsonEnvelope query = envelope().with(
                metadataOf(randomUUID(), "example.ovens-status"))
                .build();

        final JsonEnvelope status = ovenCustomApi.status(query);

        assertThat(status, jsonEnvelope()
                .withMetadataOf(metadata()
                        .withName("example.ovens-status"))
                .withPayloadOf(
                        payloadIsJson(
                                allOf(
                                        withJsonPath("$.ovens[0].id", notNullValue()),
                                        withJsonPath("$.ovens[0].name", equalTo("Big Oven")),
                                        withJsonPath("$.ovens[0].temperature", equalTo(250)),
                                        withJsonPath("$.ovens[0].active", equalTo(true)),
                                        withJsonPath("$.ovens[1].id", notNullValue()),
                                        withJsonPath("$.ovens[1].name", equalTo("Large Oven")),
                                        withJsonPath("$.ovens[1].temperature", equalTo(0)),
                                        withJsonPath("$.ovens[1].active", equalTo(false))
                                )
                        ))
                .thatMatchesSchema());
    }
}