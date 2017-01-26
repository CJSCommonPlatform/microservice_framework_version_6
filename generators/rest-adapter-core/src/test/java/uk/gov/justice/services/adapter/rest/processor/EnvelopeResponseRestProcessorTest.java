package uk.gov.justice.services.adapter.rest.processor;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.emptyList;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link RestProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class EnvelopeResponseRestProcessorTest {

    private static final Collection<Parameter> NOT_USED_PATH_PARAMS = emptyList();
    private static final HttpHeaders NOT_USED_HEADERS = new ResteasyHttpHeaders(new MultivaluedMapImpl<>());
    private static final String NOT_USED_ACTION = "actionABC";

    @Mock
    private Consumer<JsonEnvelope> consumer;

    @Mock
    private Function<JsonEnvelope, Optional<JsonEnvelope>> function;

    private RestProcessor restProcessor;

    @Before
    public void setup() {
        restProcessor = new EnvelopeResponseRestProcessor(new RestEnvelopeBuilderFactory(), new JsonObjectEnvelopeConverter());
    }

    @Test
    public void shouldExtendBaseRestProcessor() throws Exception {
        assertThat(restProcessor, instanceOf(BaseRestProcessor.class));
    }

    @Test
    public void shouldReturnEnvelopeWithMetadata() {
        final UUID metadataId = UUID.randomUUID();
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                Optional.of(envelope().with(metadataOf(metadataId, "name1")).build()));

        final Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        with(response.getEntity().toString())
                .assertEquals(METADATA + ".name", "name1");
        assertThat(response.getHeaderString(ID), nullValue());
    }

    @Test
    public void shouldReturnOkResponseOnSyncProcessing() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(Optional.of(envelope().with(metadataWithDefaults()).build()));
        final Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    @Test
    public void shouldReturnPayloadOfEnvelopeReturnedByFunction() {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf("value33", "key11").withPayloadOf("value55", "key22").build()));

        final Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);
        with(response.getEntity().toString())
                .assertThat("key11", equalTo("value33"))
                .assertThat("key22", equalTo("value55"));
    }
}