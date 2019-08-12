package uk.gov.justice.services.jmx.util;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import javax.inject.Inject;

public class ContextNameProvider {

    @Inject
    private ServiceContextNameProvider serviceContextNameProvider;

    public String getContextName() {

        final String warName = serviceContextNameProvider.getServiceContextName();

        final int index = warName.indexOf('-');

        if (index == -1) {
            return warName;
        }

        return warName.substring(0, index);
    }
}
