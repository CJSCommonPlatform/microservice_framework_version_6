package uk.gov.justice.raml.common.validator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.raml.model.Raml;

public class CompositeRamlValidatorTest {

    @Test
    public void shouldCallAllElementsOfComposite() throws Exception {

        RamlValidator element1 = mock(RamlValidator.class);
        RamlValidator element2 = mock(RamlValidator.class);

        RamlValidator validator = new CompositeRamlValidator(element1, element2);

        Raml raml = new Raml();
        validator.validate(raml);

        verify(element1).validate(raml);
        verify(element2).validate(raml);
    }

    @Test
    public void shouldCallAllElementsOfCompositeMultipleTimes() throws Exception {
        RamlValidator element1 = mock(RamlValidator.class);
        RamlValidator element2 = mock(RamlValidator.class);

        RamlValidator validator = new CompositeRamlValidator(element1, element2);

        Raml raml = new Raml();
        validator.validate(raml);
        validator.validate(raml);

        verify(element1, times(2)).validate(raml);
        verify(element2, times(2)).validate(raml);
    }

}
