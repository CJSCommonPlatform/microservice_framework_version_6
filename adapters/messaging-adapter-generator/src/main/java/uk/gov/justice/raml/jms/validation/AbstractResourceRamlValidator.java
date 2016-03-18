package uk.gov.justice.raml.jms.validation;

import org.raml.model.Raml;
import org.raml.model.Resource;

abstract class AbstractResourceRamlValidator implements RamlValidator {

    @Override
    public void validate(final Raml raml) {
        raml.getResources().values().forEach(this::validate);
    }

    protected abstract void validate(Resource resource);

}
