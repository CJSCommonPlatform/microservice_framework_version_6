package uk.gov.justice.services.jmx.lifecycle;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.ejb.Singleton;

@Singleton
@Startup
public class ShutteringFlagProducerBean {

    private boolean doShuttering;

    @PostConstruct
    public void init(){
        doShuttering = false;
    }

    public boolean isDoShuttering() {
        return doShuttering;
    }

    public void setDoShuttering(final boolean doShuttering) {
        this.doShuttering = doShuttering;
    }
}
