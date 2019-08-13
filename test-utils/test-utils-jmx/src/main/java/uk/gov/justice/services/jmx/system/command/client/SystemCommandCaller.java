package uk.gov.justice.services.jmx.system.command.client;

import static uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder.jmxParameters;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import com.google.common.annotations.VisibleForTesting;

public class SystemCommandCaller {

    private static final String HOST = getHost();
    private static final int JMX_PORT = 9990;
    private static final String USERNAME = "admin";

    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD = "admin";


    private final TestSystemCommanderClientFactory testSystemCommanderClientFactory;
    private final JmxParameters jmxParameters;

    public SystemCommandCaller(final String contextName) {
        this(jmxParameters()
                .withContextName(contextName)
                .withHost(HOST)
                .withPort(JMX_PORT)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .build());
    }

    public SystemCommandCaller(final JmxParameters jmxParameters) {
        this(jmxParameters, new TestSystemCommanderClientFactory());
    }

    @VisibleForTesting
    SystemCommandCaller(final JmxParameters jmxParameters, final TestSystemCommanderClientFactory testSystemCommanderClientFactory) {
        this.jmxParameters = jmxParameters;
        this.testSystemCommanderClientFactory = testSystemCommanderClientFactory;
    }


    public void callRebuild() {

        try (final SystemCommanderClient systemCommanderClient = testSystemCommanderClientFactory.create(jmxParameters)) {
            systemCommanderClient.getRemote(jmxParameters.getContextName()).call(new RebuildCommand());
        }
    }

    public void callCatchup() {

        try (final SystemCommanderClient systemCommanderClient = testSystemCommanderClientFactory.create(jmxParameters)) {
            systemCommanderClient.getRemote(jmxParameters.getContextName()).call(new CatchupCommand());
        }
    }

    public void callShutter() {

        try (final SystemCommanderClient systemCommanderClient = testSystemCommanderClientFactory.create(jmxParameters)) {
            systemCommanderClient.getRemote(jmxParameters.getContextName()).call(new ShutterCommand());
        }
    }

    public void callUnshutter() {

        try (final SystemCommanderClient systemCommanderClient = testSystemCommanderClientFactory.create(jmxParameters)) {
            systemCommanderClient.getRemote(jmxParameters.getContextName()).call(new UnshutterCommand());
        }
    }
}
