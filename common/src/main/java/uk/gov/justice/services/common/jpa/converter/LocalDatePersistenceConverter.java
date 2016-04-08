package uk.gov.justice.services.common.jpa.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;

import static java.sql.Date.valueOf;

/**
 * JPA {@link AttributeConverter} to manage date field conversion between entities and database.
 */
@Converter(autoApply = true)
public class LocalDatePersistenceConverter implements AttributeConverter<LocalDate, Date> {

    @Override
    public Date convertToDatabaseColumn(final LocalDate entityValue) {
        return entityValue != null ? valueOf(entityValue) : null;
    }

    @Override
    public LocalDate convertToEntityAttribute(final Date databaseValue) {
        return databaseValue != null ? databaseValue.toLocalDate() : null;
    }
}
