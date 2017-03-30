package uk.gov.justice.services.adapter.messaging;

import static org.mockito.Mockito.mock;

import javax.jms.TextMessage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJmsParameterCheckerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfMoreThanOneParameter() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can only be used on single argument methods");

        new DefaultJmsParameterChecker().check(new Object[]{new Object(), new Object()});
    }

    @Test
    public void shouldThrowExceptionIfLessThanOneParameter() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can only be used on single argument methods");

        new DefaultJmsParameterChecker().check(new Object[]{});
    }

    @Test
    public void shouldThrowExceptionIfSingleParameterIsNotTextMessageObject() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can only be used on a JMS TextMessage, not java.lang.Object");

        new DefaultJmsParameterChecker().check(new Object[]{new Object()});
    }

    @Test
    public void shouldNotThrowExceptionIfSingleParameterIsTextMessageObject() throws Exception {
        new DefaultJmsParameterChecker().check(new Object[]{mock(TextMessage.class)});
    }
}