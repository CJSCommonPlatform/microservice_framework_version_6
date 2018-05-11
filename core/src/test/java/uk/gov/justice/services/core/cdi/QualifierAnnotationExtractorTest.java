package uk.gov.justice.services.core.cdi;

import static com.google.common.collect.ImmutableSet.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QualifierAnnotationExtractorTest {

    @InjectMocks
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Test
    public void shouldGetTheSubscriptionNameQualifierFromAnEventListenerClass() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final Annotation annotation = DummyEventListener.class.getDeclaredAnnotation(Named.class);

        when(injectionPoint.getQualifiers()).thenReturn(of(annotation));

        final Named named = qualifierAnnotationExtractor.getFrom(injectionPoint, Named.class);

        assertThat(named.value(), is("Dummy"));
    }

    @Test
    public void shouldThrowARuntimeExceptionIfNoSubscriptionNameQualifierOnAnEventListener() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class, RETURNS_DEEP_STUBS.get());

        final Set<Annotation> emptySet = new HashSet<>();

        when(injectionPoint.getQualifiers()).thenReturn(emptySet);
        when(injectionPoint.getBean().getName()).thenReturn(DummyEventListener.class.getName());

        try {
            qualifierAnnotationExtractor.getFrom(injectionPoint, Named.class);
            fail();
        } catch (final InjectionException expected) {
            assertThat(expected.getMessage(), is("Failed to find 'javax.inject.Named' annotation on bean 'uk.gov.justice.services.core.cdi.QualifierAnnotationExtractorTest$DummyEventListener'"));
        }
    }

    @Named("Dummy")
    public static class DummyEventListener {

        @Handles("my-context.something-happened")
        public void somethingHappened(final JsonEnvelope event) {

        }
    }
}

