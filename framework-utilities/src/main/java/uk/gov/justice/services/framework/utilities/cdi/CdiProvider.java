package uk.gov.justice.services.framework.utilities.cdi;

import javax.enterprise.inject.spi.CDI;

public class CdiProvider {

    public CDI<Object> getCdi() {
        return CDI.current();
    }
}
