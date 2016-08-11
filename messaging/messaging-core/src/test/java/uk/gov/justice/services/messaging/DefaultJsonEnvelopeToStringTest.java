package uk.gov.justice.services.messaging;

import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonassert.impl.matcher.IsEmptyCollection.empty;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;

import java.io.IOException;
import java.util.UUID;

import com.google.common.io.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJsonEnvelopeToStringTest {

    private static final String NAME_VALUE = "test.command.do-something";


    private static final UUID CAUSE_1_VALUE = UUID.fromString("cd68037b-2fcf-4534-b83d-a9f08072f2ca");
    private static final UUID CAUSE_2_VALUE = UUID.fromString("43464b22-04c1-4d99-8359-82dc1934d763");
    private static final UUID ID_VALUE = UUID.fromString("861c9430-7bc6-4bf0-b549-6534394b8d65");
    private static final String CLIENT_CORRELTAION_ID_VALUE = "d51597dc-2526-4c71-bd08-5031c79f11e1";
    private static final String SESSION_ID_VALUE = "45b0c3fe-afe6-4652-882f-7882d79eadd9";
    private static final String USER_ID_VALUE = "72251abb-5872-46e3-9045-950ac5bae399";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SESSION = "session";
    private static final String CORRELATION = "correlation";
    private static final String CAUSATION = "causation";
    private static final String USER = "user";

    private final JsonEnvelope envelopeWithCausation = loadFromClasspath("envelope.json");
    private final JsonEnvelope envelopeWithoutCausation = loadFromClasspath("envelope-without-causations.json");
    private final JsonEnvelope envelopeWithoutOptionals = loadFromClasspath("envelope-without-optionals.json");

    @Test
    public void shouldPrintAsTraceWithoutCausations() throws Exception {
        final String json = envelopeWithoutCausation.toString();
        with(json)
                .assertEquals(ID, ID_VALUE.toString())
                .assertEquals(NAME, NAME_VALUE)
                .assertEquals(SESSION, SESSION_ID_VALUE)
                .assertEquals(CORRELATION, CLIENT_CORRELTAION_ID_VALUE)
                .assertThat(CAUSATION, empty());
    }

    @Test
    public void shouldPrintAsTrace() throws Exception {
        final String json = envelopeWithCausation.toString();
        with(json)
                .assertEquals(ID, ID_VALUE.toString())
                .assertEquals(NAME, NAME_VALUE)
                .assertEquals(USER, USER_ID_VALUE)
                .assertEquals(SESSION, SESSION_ID_VALUE)
                .assertEquals(CORRELATION, CLIENT_CORRELTAION_ID_VALUE)
                .assertThat(CAUSATION, hasItems(CAUSE_1_VALUE.toString(), CAUSE_2_VALUE.toString()));
    }

    @Test
    public void shouldPrintWithoutMissingOptionals() throws Exception {
        with(envelopeWithoutOptionals.toString())
                .assertEquals(ID, ID_VALUE.toString())
                .assertEquals(NAME, NAME_VALUE)
                .assertNotDefined(SESSION, SESSION_ID_VALUE)
                .assertNotDefined(CORRELATION, CLIENT_CORRELTAION_ID_VALUE);
    }

    private static JsonEnvelope loadFromClasspath(String fileName) {
        final String resourceName = "json/" + fileName;
        try {
            final String jsonString = Resources.toString(getResource(resourceName), defaultCharset());
            return new JsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter().convert(jsonString));
        } catch (IOException e) {
            throw new AssertionError("Failed to read json file '" + resourceName + "' from classpath", e);
        }
    }
}
