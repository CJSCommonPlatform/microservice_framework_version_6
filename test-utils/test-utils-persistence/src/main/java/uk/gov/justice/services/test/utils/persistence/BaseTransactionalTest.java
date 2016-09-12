package uk.gov.justice.services.test.utils.persistence;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Before;

/**
 * This class should be extended by any test which require managed persistence/transactions
 * provided by deltaspike via JPA.
 */
public abstract class BaseTransactionalTest {

    @Inject
    UserTransaction userTransaction;

    @Before
    public final void setup() throws SystemException, NotSupportedException {
        userTransaction.begin();
        setUpBefore();
    }

    @After
    public final void tearDown() throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException {
        tearDownAfter();
        userTransaction.commit();
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
