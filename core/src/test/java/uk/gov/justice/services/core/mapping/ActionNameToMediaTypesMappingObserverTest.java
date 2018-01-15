package uk.gov.justice.services.core.mapping;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.annotation.MediaTypesMapper;

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
public class ActionNameToMediaTypesMappingObserverTest {

    @InjectMocks
    private ActionNameToMediaTypesMappingObserver actionNameToMediaTypesMappingObserver;

    @Test
    public void shouldRegisterAnnotatedMediaTypesMappers() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(Object.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(TestMediaTypesMapperAction.class);
        when(bean_2.getBeanClass()).thenReturn(TestMediaTypesMapperAction.class);

        actionNameToMediaTypesMappingObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<ActionNameToMediaTypesMapper>> interceptorBeans = actionNameToMediaTypesMappingObserver.getNameMediaTypesMappers();

        assertThat(interceptorBeans.size(), is(2));
        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }

    @Test
    public void shouldOnlyRegisterAnnotatedMediaTypesMappers() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(Object.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(TestMediaTypesMapperAction.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        actionNameToMediaTypesMappingObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<ActionNameToMediaTypesMapper>> interceptorBeans = actionNameToMediaTypesMappingObserver.getNameMediaTypesMappers();

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
        when(bean_1.getBeanClass()).thenReturn(DodgyMediaTypesMapper.class);

        try {
            actionNameToMediaTypesMappingObserver.afterDeploymentValidation(event, beanManager);
            fail();
        } catch (final BadMediaTypesMapperAnnotationException expected) {
            assertThat(expected.getMessage(), is(
                    "Class 'uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMappingObserverTest$DodgyMediaTypesMapper' " +
                            "annotated with @MediaTypesMapper " +
                            "should implement the 'uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMapper' interface"));
        }
    }

    @MediaTypesMapper
    public class TestMediaTypesMapperAction implements ActionNameToMediaTypesMapper {
        @Override
        public Map<String, MediaTypes> getActionNameToMediaTypesMap() {
            throw new UnsupportedOperationException();
        }
    }

    @MediaTypesMapper
    public class DodgyMediaTypesMapper {
    }
}
