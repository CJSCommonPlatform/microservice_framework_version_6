package uk.gov.justice.services.example.cakeshop.it;

import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;

import org.junit.Test;

public class SetupDatabaseTest {

    @Test
    public void shouldSetupDatabase() throws Exception {
        new CakeShopRepositoryManager().initialise();
    }
}
