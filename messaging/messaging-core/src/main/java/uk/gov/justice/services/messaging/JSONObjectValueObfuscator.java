package uk.gov.justice.services.messaging;


import java.math.BigDecimal;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Obfuscates values of {@link JSONObject}. Keeps the json structure
 */
public class JSONObjectValueObfuscator {

    private static final String OBFUSCATED_STRING = "xxx";
    private static final Boolean OBFUSCATED_BOOLEAN = false;
    private static final String OBFUSCATED_UUID = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
    private static final Integer OBFUSCATED_NUMERIC = 0;

    /**
     * Obfuscates values in the passed json
     *
     * @param json the JSONObject to obfuscate
     * @return new json with obfuscatedObject values
     */
    public static JSONObject obfuscated(final JSONObject json) {
        return (JSONObject) obfuscatedObject(json);
    }

    private static Object obfuscatedObject(final Object object) {
        if (isJSONObject(object)) {
            final JSONObject jsonObject = (JSONObject) object;
            final JSONObject obfuscatedJsonObject = new JSONObject();
            jsonObject.keys()
                    .forEachRemaining(key -> obfuscatedJsonObject.put(key, obfuscatedObject(jsonObject.get(key))));
            return obfuscatedJsonObject;
        } else if (isArray(object)) {
            final JSONArray jsonArray = (JSONArray) object;
            final JSONArray obfuscatedJsonArray = new JSONArray();
            jsonArray.iterator().forEachRemaining(o -> obfuscatedJsonArray.put(obfuscatedObject(o)));
            return obfuscatedJsonArray;
        } else {
            return obfuscatedValueOf(object);
        }
    }

    private static Object obfuscatedValueOf(final Object object) {
        if (isABoolean(object)) {
            return OBFUSCATED_BOOLEAN;
        }
        if (isNumeric(object)) {
            return OBFUSCATED_NUMERIC;
        }
        if (isUuid(object)) {
            return OBFUSCATED_UUID;
        }
        return OBFUSCATED_STRING;
    }

    private static boolean isJSONObject(final Object object) {
        return object instanceof JSONObject;
    }

    private static boolean isArray(final Object object) {
        return object instanceof JSONArray;
    }

    private static boolean isABoolean(final Object object) {
        return object instanceof Boolean;
    }


    private static boolean isNumeric(final Object object) {
        try {
            new BigDecimal(object.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isUuid(final Object object) {
        return object instanceof UUID;
    }
}
