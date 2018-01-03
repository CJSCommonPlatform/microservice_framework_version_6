package uk.gov.justice.services.core.mapping;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.annotation.AnyLiteral;
import uk.gov.justice.services.core.annotation.MediaTypesMapper;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.slf4j.Logger;

/**
 * Collects all {@link ActionNameToMediaTypesMapper} beans that are annotated with
 * {@link MediaTypesMapper} after deployment, and allows access to list.
 */
public class ActionNameToMediaTypesMappingObserver implements Extension {

    private static final Logger LOGGER = getLogger(ActionNameToMediaTypesMappingObserver.class);

    private final List<Bean<ActionNameToMediaTypesMapper>> nameToMediaTypesMappers = new ArrayList<>();

    @SuppressWarnings({"unused", "unchecked"})
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        beanManager.getBeans(Object.class, AnyLiteral.create()).stream()
                .filter(this::isMediaTypesMapperMapper)
                .peek(this::logNameToMediaTypesMapper)
                .forEach(bean -> nameToMediaTypesMappers.add((Bean<ActionNameToMediaTypesMapper>) bean));
    }

    /**
     * List of {@link MediaTypeToSchemaIdMapper} Beans that were identified after deployment.
     *
     * @return List of {@link MediaTypeToSchemaIdMapper} Beans
     */
    List<Bean<ActionNameToMediaTypesMapper>> getNameMediaTypesMappers() {
        return nameToMediaTypesMappers;
    }

    private boolean isMediaTypesMapperMapper(final Bean<?> bean) {

        if (bean.getBeanClass().isAnnotationPresent(MediaTypesMapper.class)) {
            if (!ActionNameToMediaTypesMapper.class.isAssignableFrom(bean.getBeanClass())) {

                final String message = format("Class '%s' annotated with @%s should implement the '%s' interface",
                        bean.getBeanClass().getName(),
                        MediaTypesMapper.class.getSimpleName(),
                        ActionNameToMediaTypesMapper.class.getName());

                throw new BadMediaTypesMapperAnnotationException(message);
            }

            return true;
        }

        return false;
    }

    private void logNameToMediaTypesMapper(final Bean<?> bean) {
        LOGGER.info("Found MediaTypesMapper {}", bean.getBeanClass().getSimpleName());
    }
}
