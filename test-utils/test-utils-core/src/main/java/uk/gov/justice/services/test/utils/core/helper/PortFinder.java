package uk.gov.justice.services.test.utils.core.helper;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Utility class to check is a port if available and return an open port. <br>
 * Try to get a port if its already in use try a random port between 8000 and 9000
 * <br>
 * Example usage: <br>
 * <code>
 * WireMockRule wireMock8080 = new WireMockRule(
 *                                  PortFinder.getPortWithRandomPortFallback(8080));
 * </code>
 * 
 *
 */
public final class PortFinder {

    /**
     * Private constructor on utility class to prevent instantiation
     */
    private PortFinder() {}

    /**
     * Check if a port is available and return the same, if available. <br>
     * 
     * If the specified port is not available try a random port from<br>
     * 8000 to 9000 a random number of tries between 10 and 20<br>
     * 
     * If none of the above works return the passed in port
     * 
     * @param port
     * @return
     */
    public static final int getPortWithRandomPortFallback(final int port) {
        int allocatedport = getPort(port);
        if (allocatedport != (-1)) {
            return allocatedport;
        }
        final int[] array = new Random().ints(randomInRange(10, 20), 8000, 9000).toArray();
        allocatedport = getPortFromArray(array);
        if (allocatedport != (-1)) {
            return allocatedport;
        }
        return port;
    }

    /**
     * Get a random number in a range of numbers
     * 
     * @param min number of range
     * @param max number of range
     * @return random number in range
     */
    private static int randomInRange(final int min, final int max) {
        final int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    /**
     * Select an available port from an array of ports
     * 
     * @param ports of ports to try
     * @return available port or -1 if all ports are unavailable
     */
    private static final int getPortFromArray(final int[] ports) {
        int port = -1;
        for (final int p : ports) {
            try {
                port = getPort(p);
                if (port != -1) {
                    return port;
                }
            } catch (Exception ioe) {
                port = -1;
            }
        }
        return port;
    }

    /**
     * Get port if available
     * 
     * @param sport supplied port
     * @return supplied port or -1 if unavailable
     */
    private static final int getPort(final int sport) {
        int port = -1;
        try {
            port = checkPort(sport);
        } catch (Exception ioe) {
            port = -1;
        }
        return port;
    }

    /**
     * Check if port is available
     * 
     * @param port to check
     * @return port if available
     * @throws IOException if unsuccessful
     */
    private static int checkPort(final int port) throws IOException {
        try (final ServerSocket s = new ServerSocket(port)) {
            return s.getLocalPort();
        }
    }
}
