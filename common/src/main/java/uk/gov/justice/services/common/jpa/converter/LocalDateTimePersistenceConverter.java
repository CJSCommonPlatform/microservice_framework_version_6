package uk.gov.justice.services.common.jpa.converter;

import static java.sql.Timestamp.valueOf;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA {@link AttributeConverter} to manage date time field conversion between entities and
 * database.
 */
@Converter(autoApply = true)
public class LocalDateTimePersistenceConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(final LocalDateTime entityValue) {
        return entityValue != null ? valueOf(entityValue) : null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(final Timestamp databaseValue) {
        return databaseValue != null ? databaseValue.toLocalDateTime() : null;
    }
}