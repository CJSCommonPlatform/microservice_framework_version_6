package uk.gov.justice.services.test.utils.core.matchers;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

public class ServiceComponentTestClasses {

    public static final String CUSTOM_API = "CUSTOM_API";

    @ServiceComponent(COMMAND_API)
    public static class ValidCommandApi {

        @Inject
        Sender sender;

        @Handles("context.commandA")
        public void testA(final JsonEnvelope command) {
            sender.send(command);
        }

        @Handles("context.commandB")
        public void testB(final JsonEnvelope command) {
            sender.send(command);
        }
    }

    public static class NoServiceComponentAnnotation {

        @Inject
        Sender sender;

        @Handles("context.commandA")
        public void testA(final JsonEnvelope command) {
            sender.send(command);
        }
    }

    @ServiceComponent(COMMAND_API)
    public static class NoHandlerMethod {

        @Inject
        Sender sender;
    }

    @CustomServiceComponent(CUSTOM_API)
    public static class ValidCustomServiceComponent {

        @Inject
        Sender sender;

        @Handles("context.commandA")
        public void testA(final JsonEnvelope command) {
            sender.send(command);
        }

        @Handles("context.commandB")
        public void testB(final JsonEnvelope command) {
            sender.send(command);
        }
    }
}
