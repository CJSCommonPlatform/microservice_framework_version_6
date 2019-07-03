package uk.gov.justice.services.jmx.system.command.client.connection;

import static javax.management.remote.JMXConnector.CREDENTIALS;

import java.util.HashMap;
import java.util.Map;

public class CredentialsFactory {

    public Map<String, Object> create(final String username, final String password) {

        final String[] credentials = new String[] {
                username,
                password
        };

        final Map<String, Object> environment = new HashMap<>();
        environment.put(CREDENTIALS, credentials);

        return environment;
    }
}
