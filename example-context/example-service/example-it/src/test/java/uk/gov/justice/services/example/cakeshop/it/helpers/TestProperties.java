package uk.gov.justice.services.example.cakeshop.it.helpers;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads test.properties files from classpath and makes property values available through an
 * accessor method
 */
public class TestProperties {
    private static TestProperties instance;
    private final Properties properties = new Properties();

    public TestProperties(final String propertyFileName) {
        final InputStream is = getClass().getClassLoader().getResourceAsStream(propertyFileName);

        if (is != null) {
            try {
                properties.load(is);
            } catch (IOException e) {
                fail("error reading " + propertyFileName);
            }
        } else {
            fail(propertyFileName + "not found in the classpath");
        }
    }

    /**
     * Accessor method to read property values
     *
     * @param propertyName - name of the property defined in the properties files
     * @return value of the property
     */
    public String value(final String propertyName) {
        return properties.getProperty(propertyName);
    }

}
