package uk.gov.justice.services.test.utils.core.envelopes;

@FunctionalInterface
public interface EnvelopeGeneratorProvider {

    JsonEnvelopeGenerator getGenerator();
}
