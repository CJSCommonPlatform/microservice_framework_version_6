package uk.gov.justice.services.jmx.command;

import java.io.Serializable;

public interface SystemCommand extends Serializable {

    String getName();
    String getDescription();
}
