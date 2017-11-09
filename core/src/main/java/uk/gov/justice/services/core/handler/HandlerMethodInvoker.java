package uk.gov.justice.services.core.handler;


import static java.lang.Class.forName;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HandlerMethodInvoker {

    public Object invoke(final Object handlerInstance,
                         final Method handlerMethod,
                         final JsonEnvelope jsonEnvelope)
            throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, IOException {

        final Class<?> parameterClass = handlerMethod.getParameterTypes()[0];
        if(parameterClass == JsonEnvelope.class) {
            return handlerMethod.invoke(handlerInstance, jsonEnvelope);
        }
        final Type[] genericParameterTypes = handlerMethod.getGenericParameterTypes();
        final Type[] parameters = ((ParameterizedType)genericParameterTypes[0]).getActualTypeArguments();

        final Class<?> envelopeType = forName(parameters[0].getTypeName());

        final JsonObject jsonObject = jsonEnvelope.payloadAsJsonObject();

        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

        final Object readValue = objectMapper.readValue(jsonObject.toString(), envelopeType);

        final Envelope<Object> envelope = (Envelope.envelopeFrom(jsonEnvelope.metadata(), readValue));

        return handlerMethod.invoke(handlerInstance, envelope);
    }
}
