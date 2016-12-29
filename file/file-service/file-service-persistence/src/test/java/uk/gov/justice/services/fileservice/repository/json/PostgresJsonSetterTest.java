package uk.gov.justice.services.fileservice.repository.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.fileservice.repository.json.PostgresJsonSetter;

import java.sql.PreparedStatement;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.postgresql.util.PGobject;

@RunWith(MockitoJUnitRunner.class)
public class PostgresJsonSetterTest {

    @Captor
    ArgumentCaptor<PGobject> pgObjectCaptor;

    @InjectMocks
    private PostgresJsonSetter postgresJsonSetter;

    @Test
    public void shouldSetJsonOnTheCorrectColumnUsingThePostgresSpecificPGObjectClass() throws Exception {

        final String json = "{\"some\": \"json\"}";
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final JsonObject jsonObject = mock(JsonObject.class);

        when(jsonObject.toString()).thenReturn(json);

        postgresJsonSetter.setJson(preparedStatement, jsonObject);

        verify(preparedStatement).setObject(eq(2), pgObjectCaptor.capture());

        assertThat(pgObjectCaptor.getValue().getType(), is("json"));
        assertThat(pgObjectCaptor.getValue().getValue(), is(json));
    }
}
