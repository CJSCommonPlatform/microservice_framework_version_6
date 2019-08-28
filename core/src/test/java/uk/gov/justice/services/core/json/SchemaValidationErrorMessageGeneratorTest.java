package uk.gov.justice.services.core.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaValidationErrorMessageGeneratorTest {


    @InjectMocks
    private SchemaValidationErrorMessageGenerator schemaValidationErrorMessageGenerator;

    @Test
    public void shouldName() throws Exception {

        final Schema violatedSchema = mock(Schema.class);

        when(violatedSchema.getId()).thenReturn("schema id");

        final ValidationException validationException = new ValidationException(
                violatedSchema,
                "error message",
                "don't care",
                "schemaLocation");

        final String errorMessage = schemaValidationErrorMessageGenerator.generateErrorMessage(validationException);

        assertThat(errorMessage, is("Json validation failed with 1 violation(s): error message. Errors: [#: error message]. Violated schema id: 'schema id'. Location: 'schemaLocation'"));
    }
}
