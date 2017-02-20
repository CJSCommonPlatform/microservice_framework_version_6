package uk.gov.justice.services.common.configuration;

import static java.lang.String.format;
import static org.hamcrest.core.IsEqual.equalTo;
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
public class GlobalValueProducerTest {

    @InjectMocks
    private GlobalValueProducer valueProducer;

    @Mock
    private InjectionPoint propertyInjectionPoint;

    @Mock
    private Annotated annotated;

    @Mock
    private GlobalValue annotation;

    @Mock
    InitialContext initialContext;

    @Before
    public void setup() throws NamingException {
        when(propertyInjectionPoint.getAnnotated()).thenReturn(annotated);
        when(annotated.getAnnotation(GlobalValue.class)).thenReturn(annotation);

    }

    @Test
    public void shouldReturnStringPropertyValue() throws NamingException {
        when(initialContext.lookup("java:global/myProperty")).thenReturn("some value");
        when(annotation.key()).thenReturn("myProperty");

        assertThat(valueProducer.stringValueOf(propertyInjectionPoint), equalTo("some value"));
    }

    @Test
    public void shouldReturnLongPropertyValue() throws NamingException {
        when(initialContext.lookup("java:global/myLongProperty")).thenReturn("100");
        when(annotation.key()).thenReturn("myLongProperty");
        assertThat(valueProducer.longValueOf(propertyInjectionPoint), equalTo(100L));
    }

    @Test
    public void shouldReturnDefaultValueWhenNotFound() throws NamingException {
        when(initialContext.lookup("java:global/myOtherProperty")).thenThrow(NameNotFoundException.class);
        when(annotation.key()).thenReturn("myOtherProperty");
        when(annotation.defaultValue()).thenReturn("some default value");

        assertThat(valueProducer.stringValueOf(propertyInjectionPoint), equalTo("some default value"));
    }

    @Test(expected = MissingPropertyException.class)
    public void shouldThrowExceptionWhenNotFoundAndNoDefaultValue() throws NamingException {
        when(initialContext.lookup("java:global/unknownProperty")).thenThrow(NameNotFoundException.class);
        when(annotation.key()).thenReturn("unknownProperty");

        when(annotation.defaultValue()).thenReturn(CommonValueAnnotationDef.NULL_DEFAULT);

        valueProducer.stringValueOf(propertyInjectionPoint);
    }



}