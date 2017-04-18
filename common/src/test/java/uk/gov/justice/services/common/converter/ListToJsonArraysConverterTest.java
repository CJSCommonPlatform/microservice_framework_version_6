package uk.gov.justice.services.common.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.justice.services.common.converter.exception.ConverterException;

@RunWith(MockitoJUnitRunner.class)
public class ListToJsonArraysConverterTest {
	private static final UUID ID_ONE = UUID.randomUUID();
	private static final UUID ID_TWO = UUID.randomUUID();
	private static final String NAME_ONE = "POJOONE";
	private static final String NAME_TWO = "POJOTWO";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock
	private ObjectMapper mapperMock;

	@Test
	public void shouldConvertListToJsonArray() {
		ListToJsonArraysConverter<Pojo> listToJsonArraysConverter = new ListToJsonArraysConverter<Pojo>();
		listToJsonArraysConverter.mapper = new JacksonMapperProducer().objectMapper();
		listToJsonArraysConverter.stringToJsonObjectConverter = new StringToJsonObjectConverter();

		Pojo pojoOne = new Pojo(ID_ONE, NAME_ONE);
		Pojo pojoTwo = new Pojo(ID_TWO, NAME_TWO);
		JsonArray jsonArray = listToJsonArraysConverter.convert(Arrays.asList(pojoOne, pojoTwo));
		assertThat(jsonArray, equalTo(expectedJsonArray()));

	}

	@Test
	public void shouldThrowExceptionOnConversionError() {
		ListToJsonArraysConverter<Pojo> listToJsonArraysConverter = new ListToJsonArraysConverter<Pojo>();
		exception.expect(ConverterException.class);
		listToJsonArraysConverter.convert(null);

	}

	@Test
	public void shouldThrowIllegalArgumentException() throws JsonProcessingException {
		ListToJsonArraysConverter<Pojo> listToJsonArraysConverter = new ListToJsonArraysConverter<Pojo>();
		listToJsonArraysConverter.mapper = mapperMock;
		listToJsonArraysConverter.stringToJsonObjectConverter = new StringToJsonObjectConverter();

		Pojo pojoOne = new Pojo(null, null);
		doThrow(JsonProcessingException.class).when(mapperMock).writeValueAsString(pojoOne);

		exception.expect(IllegalArgumentException.class);
		exception.expectCause(isA(JsonProcessingException.class));

		listToJsonArraysConverter.convert(Arrays.asList(pojoOne));

	}

	private JsonArray expectedJsonArray() {
		JsonObject one = Json.createObjectBuilder().add("id", ID_ONE.toString()).add("name", NAME_ONE).build();
		JsonObject two = Json.createObjectBuilder().add("id", ID_TWO.toString()).add("name", NAME_TWO).build();

		JsonArray array = Json.createArrayBuilder().add(one).add(two).build();

		return array;
	}

	public static class Pojo {

		private final UUID id;
		private final String name;

		public Pojo(UUID id, String name) {
			this.id = id;
			this.name = name;
		}

		public UUID getId() {
			return id;
		}

		public String getName() {
			return name;
		}

	}
}
