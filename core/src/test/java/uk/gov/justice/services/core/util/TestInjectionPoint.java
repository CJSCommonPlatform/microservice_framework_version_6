package uk.gov.justice.services.core.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class TestInjectionPoint implements InjectionPoint {

    private final Member member;

    public TestInjectionPoint(final Member member) {
        this.member = member;
    }

    public TestInjectionPoint(final Class clazz) {
        this.member = clazz.getMethods()[0];
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return null;
    }

    @Override
    public Bean<?> getBean() {
        return null;
    }

    @Override
    public Member getMember() {
        return member;
    }

    @Override
    public Annotated getAnnotated() {
        return null;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}