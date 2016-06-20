package uk.gov.justice.services.generators.commons.validator;

import org.raml.model.Raml;
import org.raml.model.Resource;

public abstract class AbstractResourceRamlValidator implements RamlValidator {

    @Override
    public void validate(final Raml raml) {
        raml.getResources().values().forEach(this::validate);
    }

    protected abstract void validate(Resource resource);

}
