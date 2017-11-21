package uk.gov.justice.services.core.mapping;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.annotation.AnyLiteral;
import uk.gov.justice.services.core.annotation.SchemaIdMapper;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.slf4j.Logger;

/**
 * Collects all {@link MediaTypeToSchemaIdMapper} beans that are annotated with
 * {@link SchemaIdMapper} after deployment, and allows access to list.
 */
public class SchemaIdMappingObserver implements Extension {

    private static final Logger LOGGER = getLogger(SchemaIdMappingObserver.class);

    private final List<Bean<MediaTypeToSchemaIdMapper>> mediaTypeToSchemaIdMappers = new ArrayList<>();

    @SuppressWarnings({"unused", "unchecked"})
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        beanManager.getBeans(Object.class, AnyLiteral.create()).stream()
                .filter(this::isSchemaIdMapper)
                .peek(this::logMediaTypeToSchemaIdMapper)
                .forEach(bean -> mediaTypeToSchemaIdMappers.add((Bean<MediaTypeToSchemaIdMapper>) bean));
    }

    /**
     * List of {@link MediaTypeToSchemaIdMapper} Beans that were identified after deployment.
     *
     * @return List of {@link MediaTypeToSchemaIdMapper} Beans
     */
    List<Bean<MediaTypeToSchemaIdMapper>> getMediaTypeToSchemaIdMappers() {
        return mediaTypeToSchemaIdMappers;
    }

    private boolean isSchemaIdMapper(final Bean<?> bean) {

        if (bean.getBeanClass().isAnnotationPresent(SchemaIdMapper.class)) {
            if (!MediaTypeToSchemaIdMapper.class.isAssignableFrom(bean.getBeanClass())) {

                final String message = format("Class '%s' annotated with @%s should implement the '%s' interface",
                        bean.getBeanClass().getName(),
                        SchemaIdMapper.class.getSimpleName(),
                        MediaTypeToSchemaIdMapper.class.getName());

                throw new BadSchemaIdMapperAnnotationException(message);
            }

            return true;
        }

        return false;
    }

    private void logMediaTypeToSchemaIdMapper(final Bean<?> bean) {
        LOGGER.info("Found SchemaIdMapper {}", bean.getBeanClass().getSimpleName());
    }
}
