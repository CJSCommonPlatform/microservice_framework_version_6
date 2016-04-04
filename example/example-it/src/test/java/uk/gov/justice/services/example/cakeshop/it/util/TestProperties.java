package uk.gov.justice.services.example.cakeshop.it.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * Loads test.properties files from classpath and makes property values available through an accessor method
 */
public class TestProperties {
    private static TestProperties instance;
    private static final String PROPERTY_FILE_NAME = "test.properties";
    private final Properties properties = new Properties();

    private TestProperties() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME);

        if (is != null) {
            try {
                properties.load(is);
            } catch (IOException e) {
                fail("error reading " + PROPERTY_FILE_NAME);
            }
        } else {
            fail(PROPERTY_FILE_NAME + "not found in the classpath");
        }
    }

    /**
     * Singleton method
     *
     * @return instance of the class
     */
    public static TestProperties getInstance() {
        if (instance == null) {
            instance = new TestProperties();
        }
        return instance;
    }

    /**
     * Accessor method to read property values
     *
     * @param propertyName - name of the property defined in the properties files
     * @return value of the property
     */
    public String value(String propertyName) {
        return properties.getProperty(propertyName);
    }

}
