package uk.gov.justice.services.common.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JacksonMapperProducerTest {

    @Test
    public void shouldReturnAMapper() throws Exception {
        ObjectMapper mapper = new JacksonMapperProducer().objectMapper();
        assertThat(mapper, notNullValue());
        assertThat(mapper, isA(ObjectMapper.class));
    }

}