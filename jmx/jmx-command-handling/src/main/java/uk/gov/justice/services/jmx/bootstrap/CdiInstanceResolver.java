package uk.gov.justice.services.jmx.bootstrap;

import static java.lang.String.format;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class CdiInstanceResolver {

    /**
     * Gets an instance of an Object of the specified class from CDI
     *
     * @param beanClass The class of the required Object instance
     * @param beanManager The CDI BeanManager
     * @param <T> The type of the class
     * @return A fully injected instance of the required Object
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstanceOf(final Class<T> beanClass, final BeanManager beanManager) {

        try {
            final Bean<?> bean = beanManager.resolve(beanManager.getBeans(beanClass));
            final CreationalContext<?> context = beanManager.createCreationalContext(bean);
            return (T) beanManager.getReference(
                    bean,
                    beanClass,
                    context);
        } catch (final Exception e) {
            throw new UnresolvableCdiInstanceException(format("Failed to find class %s in CDI", beanClass.getName()), e);
        }
    }
}
