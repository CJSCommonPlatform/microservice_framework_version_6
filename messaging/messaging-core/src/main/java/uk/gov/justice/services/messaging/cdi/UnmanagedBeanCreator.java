package uk.gov.justice.services.messaging.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;

public class UnmanagedBeanCreator {

    @Inject
    BeanManager beanManager;

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<? extends T> aClass) {

        final Unmanaged<T> unmanaged = new Unmanaged(beanManager, aClass);
        final Unmanaged.UnmanagedInstance<T> unmanagedInstance = unmanaged.newInstance();

        return unmanagedInstance.produce().inject().get();
    }
}
