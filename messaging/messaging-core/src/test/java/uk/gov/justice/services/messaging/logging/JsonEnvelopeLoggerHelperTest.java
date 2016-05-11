package uk.gov.justice.services.messaging.logging;

import static com.jayway.jsonassert.JsonAssert.with;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class JsonEnvelopeLoggerHelperTest {

    private static final String NAME_VALUE = "test.command.do-something";


    private static final UUID CAUSE_1_VALUE = UUID.fromString("c5b56797-9f5e-4f9e-968b-88802f1e45d5");
    private static final UUID CAUSE_2_VALUE = UUID.fromString("b88d3604-9fe4-458b-b034-1b9480a20230");
    private static final UUID ID_VALUE = UUID.fromString("54e170e8-95a1-452e-b8e5-aecee51202b9");
    private static final Optional<String> CLIENT_CORRELTAION_ID_VALUE = Optional.of("e8c16418-fa1f-48f7-95cc-8dad3df04dd3");
    private static final Optional<String> SESSION_ID_VALUE = Optional.of("e938e175-075f-45c0-8ebb-7541c9253615");
    private static final Optional<String> USER_ID_VALUE = Optional.of("e647234c-7dbd-4524-aee6-d729a538926d");

    @Mock
    private JsonEnvelope envelopeWithCausation;

    @Mock
    private JsonEnvelope envelopeWithoutCausation;

    @Mock
    private JsonEnvelope envelopeWithoutOptionals;

    @Mock
    private Metadata metadata;

    @Mock
    private Metadata metadataWithoutCausation;

    @Mock
    private Metadata metadataWithoutOptionals;

    static List<UUID> makeCausations() {

        List<UUID> response = new ArrayList<>();
        response.add(CAUSE_1_VALUE);
        response.add(CAUSE_2_VALUE);
        return response;
    }

    @Before
    public void setup() {
        when(envelopeWithCausation.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME_VALUE);
        when(metadata.causation()).thenReturn(makeCausations());
        when(metadata.id()).thenReturn(ID_VALUE);
        when(metadata.clientCorrelationId()).thenReturn(CLIENT_CORRELTAION_ID_VALUE);
        when(metadata.sessionId()).thenReturn(SESSION_ID_VALUE);
        when(metadata.userId()).thenReturn(USER_ID_VALUE);

        when(envelopeWithoutCausation.metadata()).thenReturn(metadataWithoutCausation);
        when(metadataWithoutCausation.name()).thenReturn(NAME_VALUE);
        when(metadataWithoutCausation.causation()).thenReturn(null);
        when(metadataWithoutCausation.id()).thenReturn(ID_VALUE);
        when(metadataWithoutCausation.clientCorrelationId()).thenReturn(CLIENT_CORRELTAION_ID_VALUE);
        when(metadataWithoutCausation.sessionId()).thenReturn(SESSION_ID_VALUE);
        when(metadataWithoutCausation.userId()).thenReturn(Optional.empty());

        when(envelopeWithoutOptionals.metadata()).thenReturn(metadataWithoutOptionals);
        when(metadataWithoutOptionals.name()).thenReturn(NAME_VALUE);
        when(metadataWithoutOptionals.causation()).thenReturn(null);
        when(metadataWithoutOptionals.id()).thenReturn(ID_VALUE);
        when(metadataWithoutOptionals.clientCorrelationId()).thenReturn(Optional.empty());
        when(metadataWithoutOptionals.sessionId()).thenReturn(Optional.empty());
        when(metadataWithoutOptionals.userId()).thenReturn(Optional.empty());

    }

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(JsonEnvelopeLoggerHelper.class);
    }

    @Test
    public void shouldPrintAsTraceWithoutCausations() throws Exception {
        with(JsonEnvelopeLoggerHelper.toEnvelopeTraceString(envelopeWithoutCausation))
                .assertEquals(JsonObjectMetadata.ID, ID_VALUE.toString())
                .assertEquals(JsonObjectMetadata.NAME, NAME_VALUE)
                .assertEquals(JsonObjectMetadata.SESSION_ID, SESSION_ID_VALUE.get())
                .assertEquals(JsonObjectMetadata.CORRELATION, CLIENT_CORRELTAION_ID_VALUE.get())
                .assertThat(JsonObjectMetadata.CAUSATION, IsEmptyCollection.empty());
    }

    @Test
    public void shouldPrintAsTrace() throws Exception {
        with(JsonEnvelopeLoggerHelper.toEnvelopeTraceString(envelopeWithCausation))
                .assertEquals(JsonObjectMetadata.ID, ID_VALUE.toString())
                .assertEquals(JsonObjectMetadata.NAME, NAME_VALUE)
                .assertEquals(JsonObjectMetadata.USER_ID, USER_ID_VALUE.get())
                .assertEquals(JsonObjectMetadata.SESSION_ID, SESSION_ID_VALUE.get())
                .assertEquals(JsonObjectMetadata.CORRELATION, CLIENT_CORRELTAION_ID_VALUE.get())
                .assertThat(JsonObjectMetadata.CAUSATION, IsCollectionContaining.hasItems(CAUSE_1_VALUE.toString(), CAUSE_2_VALUE.toString()));
    }

    @Test
    public void shouldPrintWithoutMissingOptionals() throws Exception {
        with(JsonEnvelopeLoggerHelper.toEnvelopeTraceString(envelopeWithoutOptionals))
                .assertEquals(JsonObjectMetadata.ID, ID_VALUE.toString())
                .assertEquals(JsonObjectMetadata.NAME, NAME_VALUE)
                .assertNotDefined(JsonObjectMetadata.SESSION_ID, SESSION_ID_VALUE.get())
                .assertNotDefined(JsonObjectMetadata.CORRELATION, CLIENT_CORRELTAION_ID_VALUE.get());
    }
}