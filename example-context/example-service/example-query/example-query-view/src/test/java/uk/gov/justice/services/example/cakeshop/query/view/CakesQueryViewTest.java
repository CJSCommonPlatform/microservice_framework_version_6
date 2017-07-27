package uk.gov.justice.services.example.cakeshop.query.view;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.example.cakeshop.query.view.response.CakeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.CakesView;
import uk.gov.justice.services.example.cakeshop.query.view.service.CakeService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakesQueryViewTest {


    @Mock
    private CakeService service;

    private CakesQueryView queryView;

    @Before
    public void setUp() throws Exception {
        queryView = new CakesQueryView(service, createEnveloper());
    }

    @Test
    public void shouldHaveCorrectHandlerMethod() throws Exception {
        assertThat(queryView, isHandler(QUERY_VIEW)
                .with(method("cakes").thatHandles("example.search-cakes")));
    }

    @Test
    public void shouldReturnCakes() throws Exception {
        final UUID id1 = randomUUID();
        final String name1 = "Chocolate cake";

        final JsonEnvelope query = envelope().with(metadataWithDefaults()).build();
        final UUID id2 = randomUUID();
        final String name2 = "Cheese cake";
        when(service.cakes()).thenReturn(new CakesView(asList(new CakeView(id1, name1), new CakeView(id2, name2))));

        final JsonEnvelope response = queryView.cakes(query);

        assertThat(response, jsonEnvelope()
                .withPayloadOf(
                        payloadIsJson(
                                allOf(
                                        withJsonPath("$.cakes[0].id", equalTo(id1.toString())),
                                        withJsonPath("$.cakes[0].name", equalTo(name1)),
                                        withJsonPath("$.cakes[1].id", equalTo(id2.toString())),
                                        withJsonPath("$.cakes[1].name", equalTo(name2))
                                )

                        )));
    }
}