package uk.gov.justice.services.core.extension;

public interface EventFoundEvent {

    Class<?> getClazz();

    String getEventName();
}
