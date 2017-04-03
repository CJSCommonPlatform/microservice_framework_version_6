package uk.gov.justice.services.core.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class MemberInjectionPoint implements InjectionPoint {

    private final Member member;

    private MemberInjectionPoint(final Member member) {
        this.member = member;
    }

    public static MemberInjectionPoint injectionPointWith(final Member member) {
        return new MemberInjectionPoint(member);
    }

    public static MemberInjectionPoint injectionPointWithMemberAsFirstMethodOf(final Class clazz) {
        return new MemberInjectionPoint(clazz.getMethods()[0]);
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