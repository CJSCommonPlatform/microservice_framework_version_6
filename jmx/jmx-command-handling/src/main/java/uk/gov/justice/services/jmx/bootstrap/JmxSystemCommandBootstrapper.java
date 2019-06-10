package uk.gov.justice.services.jmx.bootstrap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

public class JmxSystemCommandBootstrapper implements Extension {

    private final ObjectFactory objectFactory;

    public JmxSystemCommandBootstrapper() {
        this(new ObjectFactory());
    }
    
    public JmxSystemCommandBootstrapper(final ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        objectFactory.systemCommandScanner().registerSystemCommands(beanManager);
    }
}
