package uk.gov.justice.services.common.converter.jackson;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_WITH_ZONE_ID;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_NULL_MAP_VALUES;

import uk.gov.justice.services.common.converter.jackson.jsr353.InclusionAwareJSR353Module;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Produces the configured {@link ObjectMapper}.
 */
@ApplicationScoped
public class ObjectMapperProducer {

    private static final String UTC = "UTC";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Produces
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule())
                .registerModule(new InclusionAwareJSR353Module())
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(WRITE_DATES_WITH_ZONE_ID, false)
                .configure(WRITE_NULL_MAP_VALUES, false)
                .setDateFormat(new SimpleDateFormat(DATE_FORMAT))
                .setTimeZone(TimeZone.getTimeZone(UTC))
                .setSerializationInclusion(NON_ABSENT);
    }
}
