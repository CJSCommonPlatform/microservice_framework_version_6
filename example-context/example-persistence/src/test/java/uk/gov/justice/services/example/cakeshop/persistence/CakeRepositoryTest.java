package uk.gov.justice.services.example.cakeshop.persistence;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Cake;
import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class CakeRepositoryTest extends BaseTransactionalTest {

    @Inject
    private CakeRepository cakeRepository;

    @Test
    public void shouldReturnCakes() throws Exception {

        final UUID id = randomUUID();
        final String name = "name123";
        final UUID id2 = randomUUID();
        final String name2 = "name456";

        cakeRepository.save(new Cake(id, name));
        cakeRepository.save(new Cake(id2, name2));

        final List<Cake> cakes = cakeRepository.findAll();
        assertThat(cakes, hasSize(2));
        assertThat(cakes.get(0).getCakeId(), is(id));
        assertThat(cakes.get(0).getName(), is(name));
        assertThat(cakes.get(1).getCakeId(), is(id2));
        assertThat(cakes.get(1).getName(), is(name2));

    }
}