package uk.gov.justice.services.test.utils.persistence;

import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Before;

/**
 * This class should be extended by any test which require managed persistence/transactions provided
 * by deltaspike via JPA.
 */
public abstract class BaseTransactionalTest {

    @Inject
    UserTransaction userTransaction;

    @Before
    public final void setup() throws Exception {
        userTransaction.begin();
        setUpBefore();
    }

    @After
    public final void tearDown() throws Exception {
        tearDownAfter();
        userTransaction.rollback();
    }

    /**
     * Implement this method if you require to do something before the test
     */
    protected void setUpBefore() {

    }

    /**
     * Implement this method if you require to do something after the test
     */
    protected void tearDownAfter() {

    }
}
