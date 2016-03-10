package uk.gov.justice.raml.jms.core;

import org.raml.model.Raml;
import uk.gov.justice.raml.core.Configuration;

import java.util.Set;

public interface Generator {
    Set<String> run(Raml raml, Configuration configuration);

}
