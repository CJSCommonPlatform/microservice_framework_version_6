package uk.gov.justice.services.test.utils.core.helper;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ServiceComponentsCommandHandlerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldVerifyAllMethodsOfAPassThroughServiceComponent() throws Exception {
        verifyPassThroughCommandHandlerMethod(ValidCommandApi.class);
    }

    @Test
    public void shouldVerifyASingleMethod() throws Exception {
        verifyPassThroughCommandHandlerMethod(ValidCommandApi.class, "testA");
    }

    @Test
    public void shouldVerifyMultipleNamedMethods() throws Exception {
        verifyPassThroughCommandHandlerMethod(ValidCommandApi.class, "testA", "testB");
    }

    @Test
    public void shouldFailWhenNoHandlerMethod() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @Handles annotation present, or no Handler methods for class InValidNoHandlerMethod");

        verifyPassThroughCommandHandlerMethod(InValidNoHandlerMethod.class);
    }

    @Test
    public void shouldFailWhenNoHandlesAnnotation() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @Handles annotation present, or no Handler methods for class InValidNoHandlesAnnotation");

        verifyPassThroughCommandHandlerMethod(InValidNoHandlesAnnotation.class);
    }

    @Test
    public void shouldFailWhenNoHandlesAnnotationOnNamedMethod() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @Handles annotation present on Method testA");

        verifyPassThroughCommandHandlerMethod(InValidNoHandlesAnnotation.class, "testA");
    }

    @Test
    public void shouldFailWhenNoServiceComponentAnnotation() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @ServiceComponent annotation present on Class NoServiceComponentAnnotation");

        verifyPassThroughCommandHandlerMethod(NoServiceComponentAnnotation.class, "testA");
    }

    @Test
    public void shouldFailWhenNoSenderField() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No field of class type Sender found in handler class");

        verifyPassThroughCommandHandlerMethod(NoSenderField.class, "testA");
    }

    @Test
    public void shouldFailWhenSenderIsNotInvoked() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("testA.sender.send");

        verifyPassThroughCommandHandlerMethod(NoPassThroughInvocation.class, "testA");
    }

    @ServiceComponent(COMMAND_API)
    public static class ValidCommandApi {

        @Inject
        Sender sender;

        @Handles("testA")
        public void testA(final JsonEnvelope command) {
            sender.send(command);
        }

        @Handles("testB")
        public void testB(final JsonEnvelope command) {
            sender.send(command);
        }
    }

    @ServiceComponent(COMMAND_API)
    public static class InValidNoHandlerMethod {

        @Inject
        Sender sender;
    }

    @ServiceComponent(COMMAND_API)
    public static class InValidNoHandlesAnnotation {

        @Inject
        Sender sender;

        public void testA(final JsonEnvelope command) {
            sender.send(command);
        }
    }

    public static class NoServiceComponentAnnotation {

        @Inject
        Sender sender;

        @Handles("testA")
        public void testA(final JsonEnvelope command) {
            sender.send(command);
        }
    }

    @ServiceComponent(COMMAND_API)
    public static class NoSenderField {

        @Handles("testA")
        public void testA(final JsonEnvelope command) {
        }
    }

    @ServiceComponent(COMMAND_API)
    public static class NoPassThroughInvocation {
        @Inject
        private Sender sender;

        @Handles("testA")
        public void testA(final JsonEnvelope command) {

        }
    }
}