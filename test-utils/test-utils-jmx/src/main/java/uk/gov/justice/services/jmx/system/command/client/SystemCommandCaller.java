package uk.gov.justice.services.jmx.system.command.client;

import static uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder.jmxParameters;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
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
        callSystemCommand(new RebuildCommand());
    }

    public void callCatchup() {
        callSystemCommand(new CatchupCommand());
    }

    public void callIndexerCatchup() {
        callSystemCommand(new IndexerCatchupCommand());
    }

    public void callShutter() {
        callSystemCommand(new ShutterCommand());
    }

    public void callUnshutter() {
        callSystemCommand(new UnshutterCommand());
    }

    private void callSystemCommand(final SystemCommand systemCommand) {
        try (final SystemCommanderClient systemCommanderClient = testSystemCommanderClientFactory.create(jmxParameters)) {
            systemCommanderClient.getRemote(jmxParameters.getContextName()).call(systemCommand);
        }
    }
}
