package uk.gov.justice.services.jmx.system.command.client;

import static uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder.jmxParameters;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;
import uk.gov.justice.services.management.suspension.commands.SuspendCommand;
import uk.gov.justice.services.management.suspension.commands.UnsuspendCommand;

import com.google.common.annotations.VisibleForTesting;

public class FrameworkSystemCommandCaller {

    private static final String HOST = getHost();
    private static final int JMX_PORT = 9990;
    private static final String USERNAME = "admin";

    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD = "admin";


    private final TestSystemCommanderClientFactory testSystemCommanderClientFactory;
    private final JmxParameters jmxParameters;

    public FrameworkSystemCommandCaller(final String contextName) {
        this(jmxParameters()
                .withContextName(contextName)
                .withHost(HOST)
                .withPort(JMX_PORT)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .build());
    }

    public FrameworkSystemCommandCaller(final JmxParameters jmxParameters) {
        this(jmxParameters, new TestSystemCommanderClientFactory());
    }

    @VisibleForTesting
    FrameworkSystemCommandCaller(final JmxParameters jmxParameters, final TestSystemCommanderClientFactory testSystemCommanderClientFactory) {
        this.jmxParameters = jmxParameters;
        this.testSystemCommanderClientFactory = testSystemCommanderClientFactory;
    }

    public void callShutter() {
        callSystemCommand(new SuspendCommand());
    }

    public void callUnshutter() {
        callSystemCommand(new UnsuspendCommand());
    }

    private void callSystemCommand(final SystemCommand systemCommand) {
        try (final SystemCommanderClient systemCommanderClient = testSystemCommanderClientFactory.create(jmxParameters)) {
            systemCommanderClient.getRemote(jmxParameters.getContextName()).call(systemCommand.getName());
        }
    }
}
