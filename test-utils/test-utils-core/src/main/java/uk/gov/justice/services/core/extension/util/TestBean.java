package uk.gov.justice.services.core.extension.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class TestBean<T> implements Bean {

    private final Class<T> beanClass;

    @SuppressWarnings("unchecked")
    public static <T> Bean<T> of(final Class<T> beanClass) {
        return new TestBean(beanClass);
    }

    private TestBean(final Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Class<T> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return null;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Object create(final CreationalContext creationalContext) {
        return null;
    }

    @Override
    public void destroy(final Object instance, final CreationalContext creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return null;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return null;
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
        return null;
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
