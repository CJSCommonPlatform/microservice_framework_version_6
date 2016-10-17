package uk.gov.justice.services.test.utils.core.helper;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Test;

public class PortFinderTest {
    
    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(PortFinder.class);
    }

    @Test
    public void shouldNotReturnSuppliedPort() {
        try (ServerSocket s = new ServerSocket(9011)) {
            int port = PortFinder.getPortWithRandomPortFallback(9011);
            assertNotEquals(9011, port);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void shouldReturnSuppliedPort() {
        try (ServerSocket s = new ServerSocket(9011)) {
            int port = PortFinder.getPortWithRandomPortFallback(9012);
            assertEquals(9012, port);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void shouldReturnRandomPortInRange() {
        for (int i = 0; i < 10; i++) {
            try (ServerSocket s = new ServerSocket(9011)) {
                int port = PortFinder.getPortWithRandomPortFallback(9011);
                assertTrue(port > 7999);
                assertTrue(port < 9001);
            } catch (IOException e) {
                fail();
            }
        }
    }
}
