package uk.gov.justice.services.core.mapping;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.annotation.SchemaIdMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaIdMappingObserverTest {

    @InjectMocks
    private SchemaIdMappingObserver schemaIdMappingObserver;

    @Test
    public void shouldRegisterAnnotatedSchemaIdMappers() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(Object.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(TestSchemaIdMapper.class);
        when(bean_2.getBeanClass()).thenReturn(TestSchemaIdMapper.class);

        schemaIdMappingObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<MediaTypeToSchemaIdMapper>> interceptorBeans = schemaIdMappingObserver.getMediaTypeToSchemaIdMappers();

        assertThat(interceptorBeans.size(), is(2));
        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }

    @Test
    public void shouldOnlyRegisterAnnotatedSchemaIdMappers() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(Object.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(TestSchemaIdMapper.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        schemaIdMappingObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<MediaTypeToSchemaIdMapper>> interceptorBeans = schemaIdMappingObserver.getMediaTypeToSchemaIdMappers();

        assertThat(interceptorBeans.size(), is(1));
        assertThat(interceptorBeans, containsInAnyOrder(bean_1));
    }

    @Test
    public void shouldFailIfTheBeanAnnotatedWithSchemaIdMapperIsNotAnInstanceOfMediaTypeToSchemaIdMapper() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);

        when(beanManager.getBeans(eq(Object.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(DodgySchemaIdMapper.class);

        try {
            schemaIdMappingObserver.afterDeploymentValidation(event, beanManager);
            fail();
        } catch (final BadSchemaIdMapperAnnotationException expected) {
            assertThat(expected.getMessage(), is(
                    "Class 'uk.gov.justice.services.core.mapping.SchemaIdMappingObserverTest$DodgySchemaIdMapper' " +
                            "annotated with @SchemaIdMapper " +
                            "should implement the 'uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper' interface"));
        }
    }

    @SchemaIdMapper
    public class TestSchemaIdMapper implements MediaTypeToSchemaIdMapper {
        @Override
        public Map<MediaType, String> getMediaTypeToSchemaIdMap() {
            throw new UnsupportedOperationException();
        }
    }

    @SchemaIdMapper
    public class DodgySchemaIdMapper {
    }
}
