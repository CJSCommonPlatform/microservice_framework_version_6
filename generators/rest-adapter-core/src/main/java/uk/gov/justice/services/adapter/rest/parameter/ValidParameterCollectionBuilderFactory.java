package uk.gov.justice.services.adapter.rest.parameter;

public class ValidParameterCollectionBuilderFactory implements ParameterCollectionBuilderFactory {

    @Override
    public ParameterCollectionBuilder create() {
        return new ValidParameterCollectionBuilder();
    }
}