package uk.gov.justice.raml.jms.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventFilterFieldCodeGeneratorTest {

    @InjectMocks
    private EventFilterFieldCodeGenerator eventFilterFieldCodeGenerator;

    @Test
    public void shouldCreateAFieldForACustomEventFilter() throws Exception {

        final ClassName customEventFilterClassName = ClassName.get("org.acme", "MyCustomEventFilter");

        final FieldSpec eventFilterField = eventFilterFieldCodeGenerator.createEventFilterField(customEventFilterClassName);

        assertThat(eventFilterField.toString(), is("@javax.inject.Inject\nprivate org.acme.MyCustomEventFilter eventFilter;\n"));
    }
}
