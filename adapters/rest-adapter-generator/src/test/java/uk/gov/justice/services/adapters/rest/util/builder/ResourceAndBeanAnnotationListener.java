package uk.gov.justice.services.adapters.rest.util.builder;

import com.sun.jersey.spi.scanning.AnnotationScannerListener;

import javax.ejb.Stateless;
import javax.ws.rs.Path;

public class ResourceAndBeanAnnotationListener extends AnnotationScannerListener {

    @SuppressWarnings("unchecked")
    public ResourceAndBeanAnnotationListener() {
        super(Path.class, Stateless.class);
    }
}
