package uk.gov.justice.raml.maven.lintchecker.rules.handlers;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Misses off @ServiceComponent to test that this handler is filtered out
 */
public class InvalidHandlers {

    @Handles("test.thirdcommand")
    public void testThirdCommand(final JsonEnvelope command) {}

}
