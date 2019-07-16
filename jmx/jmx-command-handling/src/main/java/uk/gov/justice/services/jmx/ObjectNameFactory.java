package uk.gov.justice.services.jmx;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.name.ObjectNameException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class ObjectNameFactory {

    public ObjectName create(final String domain, final String key, final String value) {
        try {
            return new ObjectName(domain, key, value);
        } catch (final MalformedObjectNameException exception) {
            throw new ObjectNameException(format(
                    "Unable to create ObjectName: domain='%s', key='%s', value='%s'",
                    domain,
                    key,
                    value), exception);
        }
    }
}
