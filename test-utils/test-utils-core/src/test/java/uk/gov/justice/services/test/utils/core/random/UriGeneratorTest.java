package uk.gov.justice.services.test.utils.core.random;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import java.lang.reflect.Field;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UriGeneratorTest {

    private static final String URI_PATTERN = "[-0-9a-zA-Z]{1,63}\\.[-.0-9a-zA-Z]+";

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldGenerateValidUris() {
        final UriGenerator uriGenerator = new UriGenerator();

        typeCheck(uriGenerator, s -> s.getScheme().equals("http") && s.getAuthority().matches(URI_PATTERN)).verify(times(10000));
    }

    @Test
    public void shouldThrowExceptionWhenGeneratedUriIsInvalid() throws Exception {
        final UriGenerator uriGenerator = new UriGenerator();

        final DomainPartGenerator domainPartGenerator = mock(DomainPartGenerator.class);
        when(domainPartGenerator.next()).thenReturn("");
        modifyUnderlyingGenerator(uriGenerator, domainPartGenerator);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Generated URI http:// is invalid");

        uriGenerator.next();
    }

    private void modifyUnderlyingGenerator(final UriGenerator uriGenerator, final DomainPartGenerator domainPartGenerator) throws NoSuchFieldException, IllegalAccessException {
        final Field field = uriGenerator.getClass().getDeclaredField("domainPartGenerator");
        field.setAccessible(true);
        field.set(uriGenerator, domainPartGenerator);
    }
}