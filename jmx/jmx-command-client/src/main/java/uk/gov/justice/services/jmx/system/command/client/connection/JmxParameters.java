package uk.gov.justice.services.jmx.system.command.client.connection;

import java.util.Objects;
import java.util.Optional;

public class JmxParameters {

    private final String host;
    private final int port;
    private final Optional<Credentials> credentials;

    public JmxParameters(final String host, final int port, final Optional<Credentials> credentials) {
        this.host = host;
        this.port = port;
        this.credentials = credentials;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Optional<Credentials> getCredentials() {
        return credentials;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof JmxParameters)) return false;
        final JmxParameters that = (JmxParameters) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, credentials);
    }

    @Override
    public String toString() {
        return "JmxParameters{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", credentials=" + credentials +
                '}';
    }
}
