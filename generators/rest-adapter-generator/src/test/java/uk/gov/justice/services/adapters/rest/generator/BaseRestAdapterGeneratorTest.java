package uk.gov.justice.services.adapters.rest.generator;


import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseRestAdapterGeneratorTest extends BaseGeneratorTest {

    @Mock
    protected RestProcessor restProcessor;

    @Before
    public void before() {
        super.before();
        generator = new RestAdapterGenerator();
    }
}
