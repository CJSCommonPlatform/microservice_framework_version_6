package uk.gov.justice.services.adapter.rest.parameter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ValidParameterCollectionBuilderFactoryTest {

    @Test
    public void shouldCreateNewInstanceOfValidParameterCollectionBuilder() throws Exception {
        final ParameterCollectionBuilder collectionBuilder = new ValidParameterCollectionBuilderFactory().create();

        assertThat(collectionBuilder, instanceOf(ValidParameterCollectionBuilder.class));
        assertThat(collectionBuilder, instanceOf(ParameterCollectionBuilder.class));
    }

    @Test
    public void shouldCreateNewInstanceForEachCreateCall() throws Exception {
        final ValidParameterCollectionBuilderFactory collectionBuilderFactory = new ValidParameterCollectionBuilderFactory();
        final ParameterCollectionBuilder collectionBuilder1 = collectionBuilderFactory.create();
        final ParameterCollectionBuilder collectionBuilder2 = collectionBuilderFactory.create();

        assertThat(collectionBuilder1, not(collectionBuilder2));
    }
}