package uk.gov.justice.raml.maven.lintchecker.rules.it.handlers;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Misses off @ServiceComponent to test that this handler is filtered out
 */
public class InvalidHandlersForTest {

    @Handles("test.no.action.for.me")
    public void testThirdCommand(final JsonEnvelope command) {}

}
