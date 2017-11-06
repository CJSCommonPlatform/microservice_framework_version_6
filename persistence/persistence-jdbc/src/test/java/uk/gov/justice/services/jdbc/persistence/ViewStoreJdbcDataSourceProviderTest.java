package uk.gov.justice.services.jdbc.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


public class ViewStoreJdbcDataSourceProviderTest {

    @Mock
    private Context initialContextMock;

    @Mock
    private DataSource dataSourceMock;

    private ViewStoreJdbcDataSourceProvider provider;

    @Before
    public void initialise() {
        initMocks(this);
    }


    private void testGetDataSourceWithProvidedWarFileNameAndExpectedResult(final String testWarFileName, final String expectedJndiName) {

        try {
            provider = new ViewStoreJdbcDataSourceProvider(testWarFileName, initialContextMock);

            when(initialContextMock.lookup(eq(expectedJndiName))).thenReturn(dataSourceMock);

            final DataSource ds = provider.getDataSource();
            assertNotNull("Returned DataSource was null!", ds);

            // Test that the correct context name is derived and used in the initialContext.lookup() call
            verify(initialContextMock, times(1)).lookup(eq(expectedJndiName));

        }
        catch (NamingException nex) {
            System.err.println(nex);
            fail("NamingException occurred !");
        }
    }

    @Test
    public void shouldReturnCorrectJndiNameWhenWarFileNameContainsAHyphen() {

        final String testWarFileName = "testing-context";
        final String expectedJndiName = "java:/DS.testing";
        testGetDataSourceWithProvidedWarFileNameAndExpectedResult(testWarFileName, expectedJndiName);
    }

    @Test
    public void shouldReturnCorrectJndiNameWhenWarFileNameDoesntContainHyphen() {

        final String testWarFileName = "testingcontext";
        final String expectedJndiName = "java:/DS.testingcontext";
        testGetDataSourceWithProvidedWarFileNameAndExpectedResult(testWarFileName, expectedJndiName);
    }
}
