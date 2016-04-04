package uk.gov.justice.services.common.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import javax.enterprise.inject.Produces;

/**
 * Produces the configured {@link ObjectMapper}
 */
public class JacksonMapperProducer {

    @Produces
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());
        mapper.registerModule(new JSR353Module());

        return mapper;
    }

}
