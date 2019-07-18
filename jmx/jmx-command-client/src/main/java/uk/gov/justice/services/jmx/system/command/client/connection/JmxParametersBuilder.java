package uk.gov.justice.services.jmx.system.command.client.connection;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

public class JmxParametersBuilder {

    private String contextName;
    private String host;
    private Integer port;
    private String username;
    private String password;

    private JmxParametersBuilder() {}

    public static JmxParametersBuilder jmxParameters() {
        return new JmxParametersBuilder();
    }

    public JmxParametersBuilder withContextName(final String contextName) {
        this.contextName = contextName;
        return this;
    }

    public JmxParametersBuilder withHost(final String host) {
        this.host = host;
        return this;
    }

    public JmxParametersBuilder withPort(final int port) {
        this.port = port;
        return this;
    }

    public JmxParametersBuilder withUsername(final String username) {
        this.username = username;
        return this;
    }

    public JmxParametersBuilder withPassword(final String password) {
        this.password = password;
        return this;
    }

    public JmxParameters build() {
        return new JmxParameters(contextName, host, port, credentials());
    }

    private Optional<Credentials> credentials() {
        if (username != null) {
            return of(new Credentials(username, password));
        }

        return empty();
    }
}
