package uk.gov.justice.services.jmx.system.command.client.connection;

import static javax.management.remote.JMXConnector.CREDENTIALS;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnvironmentFactory {

    private static final HashMap<String, Object> EMPTY_MAP = new HashMap<>();

    public Map<String, Object> create(final JmxParameters jmxParameters) {

        final Optional<Credentials> credentialsOptional = jmxParameters.getCredentials();

        if (credentialsOptional.isPresent()) {
            final Credentials credentials = credentialsOptional.get();
            final String username = credentials.getUsername();
            final String password = credentials.getPassword();

            final Map<String, Object> environment = new HashMap<>();
            environment.put(CREDENTIALS, new String[] {
                    username,
                    password
            });

            return environment;
        }

        return EMPTY_MAP;
    }
}
