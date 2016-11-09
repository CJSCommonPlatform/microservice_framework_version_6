package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DomainPartGeneratorTest {

    @Mock
    private DomainPartValidator validator;

    @Before
    public void setUp() {
        when(validator.validate(anyString())).thenReturn(true);
    }

    @Test
    public void shouldGenerateDomainParts() {
        final List<String> topLevelDomains = newArrayList("gov.uk", "co.uk", "org", "net", "com");
        final DomainPartGenerator generator = new DomainPartGenerator(topLevelDomains, validator);

        typeCheck(generator, s -> {
                    final String withoutComments = s.replace("(comment)", "");
                    final String domainName = withoutComments.substring(0, withoutComments.indexOf("."));
                    final String topLevelDomain = withoutComments.substring(withoutComments.indexOf(".") + 1);
                    return domainName.matches("[.a-zA-Z0-9-]{1,63}") && topLevelDomains.contains(topLevelDomain);
                }
        ).verify(times(10000));
    }

    @Test
    public void shouldRetryToGenerateTheDomainPartIfValidationFails() {
        when(validator.validate(anyString())).thenReturn(false).thenReturn(true);
        final DomainPartGenerator generator = new DomainPartGenerator(newArrayList("gov.uk"), validator);

        generator.next();

        verify(validator, Mockito.times(2)).validate(anyString());
    }

}
