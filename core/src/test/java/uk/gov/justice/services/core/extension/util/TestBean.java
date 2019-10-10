package uk.gov.justice.services.core.extension.util;

import static com.google.common.collect.Sets.newHashSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;


public class TestBean implements Bean<Object> {

    private final Class<?> beanClass;

    public static TestBean of(final Class<?> beanClass) {
        return new TestBean(beanClass);
    }

    private TestBean(final Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return newHashSet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Object create(CreationalContext<Object> creationalContext) {
        return null;
    }

    @Override
    public void destroy(Object o, CreationalContext<Object> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return newHashSet();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return newHashSet();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return newHashSet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        final TestBean testBean = (TestBean) o;

        return beanClass.getCanonicalName().equals(testBean.beanClass.getCanonicalName());
    }

    @Override
    public int hashCode() {
        return beanClass.getCanonicalName().hashCode();
    }
}
