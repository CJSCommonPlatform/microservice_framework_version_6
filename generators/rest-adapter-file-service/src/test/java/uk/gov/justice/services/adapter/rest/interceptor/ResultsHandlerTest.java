package uk.gov.justice.services.adapter.rest.interceptor;

import static com.google.common.collect.ImmutableMap.of;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ResultsHandlerTest {

    @InjectMocks
    private ResultsHandler resultsHandler;


    @Test
    public void shouldAddTheResultsMapToTheJsonEnvelope() throws Exception {

        final String fieldName_1 = "fieldName_1";
        final UUID fileId_1 = randomUUID();
        final String fieldName_2 = "fieldName_2";
        final UUID fileId_2 = randomUUID();

        final UUID metadataId = randomUUID();

        final String commandName = "my.command-name";
        final Map<String, UUID> results = of(fieldName_1, fileId_1, fieldName_2, fileId_2);

        final JsonEnvelope inputEnvelope = DefaultJsonEnvelope.envelope()
                .with(metadataOf(metadataId, commandName))
                .withPayloadOf("originalValue", "originalField")
                .build();

        final JsonEnvelope jsonEnvelope = resultsHandler.addResultsTo(inputEnvelope, results);

        assertThat(jsonEnvelope, is(jsonEnvelope(
                metadata()
                        .withId(metadataId)
                        .withName(commandName),
                payloadIsJson(allOf(
                        withJsonPath("$.originalField", equalTo("originalValue")),
                        withJsonPath("$.fieldName_1", equalTo(fileId_1.toString())),
                        withJsonPath("$.fieldName_2", equalTo(fileId_2.toString()))
                ))
        )));
    }
}
