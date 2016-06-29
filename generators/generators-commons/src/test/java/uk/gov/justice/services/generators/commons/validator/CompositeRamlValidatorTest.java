package uk.gov.justice.services.generators.commons.validator;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Raml;

@RunWith(MockitoJUnitRunner.class)
public class CompositeRamlValidatorTest {

    @Mock
    private RamlValidator element1;
    @Mock
    private RamlValidator element2;

    private RamlValidator validator;
    private Raml raml;

    @Before
    public void setup() {
        validator = new CompositeRamlValidator(element1, element2);
        raml = new Raml();
    }

    @Test
    public void shouldCallAllElementsOfComposite() throws Exception {
        validator.validate(raml);

        verify(element1).validate(raml);
        verify(element2).validate(raml);
    }

    @Test
    public void shouldCallAllElementsOfCompositeMultipleTimes() throws Exception {
        validator.validate(raml);
        validator.validate(raml);

        verify(element1, times(2)).validate(raml);
        verify(element2, times(2)).validate(raml);
    }

}
