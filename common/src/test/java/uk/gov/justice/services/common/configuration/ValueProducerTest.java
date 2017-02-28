package uk.gov.justice.services.common.configuration;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValueProducerTest {

    private static final String VALID_VALUE = "Valid value";
    private static final String APP_NAME = "appName";
    private static final String ANNOTATION_KEY = "somekey";
    private static final String DEFAULT_VALUE = "Default Value";
    private static final String EMPTY_VALUE = "";

    @InjectMocks
    private ValueProducer valueProducer;

    @Mock
    private InjectionPoint propertyInjectionPoint;

    @Mock
    private Annotated annotated;

    @Mock
    private Value param;

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @Mock
    InitialContext initialContext;

    @Before
    public void setup() throws NamingException {
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(APP_NAME);
        when(propertyInjectionPoint.getAnnotated()).thenReturn(annotated);
        when(annotated.getAnnotation(Value.class)).thenReturn(param);
        when(param.key()).thenReturn(ANNOTATION_KEY);
    }

    @Test
    public void shouldReturnPropertyValue() throws NamingException {
        when(initialContext.lookup(format("java:/app/%s/%s", APP_NAME, param.key()))).thenReturn(VALID_VALUE);

        assertThat(valueProducer.stringValueOf(propertyInjectionPoint), equalTo(VALID_VALUE));
    }

    @Test
    public void shouldReturnLongPropertyValue() throws NamingException {
        when(initialContext.lookup(format("java:/app/%s/%s", APP_NAME, ANNOTATION_KEY))).thenReturn("100");

        assertThat(valueProducer.longValueOf(propertyInjectionPoint), equalTo(100L));
    }

    @Test
    public void shouldReturnEmptyValue() throws NamingException {
        when(initialContext.lookup(format("java:/app/%s/%s", APP_NAME, ANNOTATION_KEY))).thenReturn(EMPTY_VALUE);

        assertThat(valueProducer.stringValueOf(propertyInjectionPoint), equalTo(EMPTY_VALUE));
    }

    @Test
    public void shouldFallBackToGlobalValueIfLocalNotFound() throws NamingException {
        when(initialContext.lookup(format("java:/app/%s/%s", APP_NAME, ANNOTATION_KEY))).thenThrow(NameNotFoundException.class);
        final String globalValue = "someValueABCD";
        when(initialContext.lookup("java:global/" + ANNOTATION_KEY)).thenReturn(globalValue);

        assertThat(valueProducer.stringValueOf(propertyInjectionPoint), equalTo(globalValue));
    }


    @Test
    public void shouldReturnDefaultValueWhenNotFound() throws NamingException {
        when(initialContext.lookup(format("java:/app/%s/%s", APP_NAME, ANNOTATION_KEY))).thenThrow(NameNotFoundException.class);
        when(initialContext.lookup("java:global/" + ANNOTATION_KEY)).thenThrow(NameNotFoundException.class);
        when(param.defaultValue()).thenReturn(DEFAULT_VALUE);

        assertThat(valueProducer.stringValueOf(propertyInjectionPoint), equalTo(DEFAULT_VALUE));
    }

    @Test(expected = MissingPropertyException.class)
    public void shouldThrowExceptionWhenNotFoundAndNoDefaultValue() throws NamingException {
        when(initialContext.lookup(format("java:/app/%s/%s", APP_NAME, ANNOTATION_KEY))).thenThrow(NameNotFoundException.class);
        when(initialContext.lookup("java:global/" + ANNOTATION_KEY)).thenThrow(NameNotFoundException.class);
        when(param.defaultValue()).thenReturn("_null_default");

        valueProducer.stringValueOf(propertyInjectionPoint);
    }

}
