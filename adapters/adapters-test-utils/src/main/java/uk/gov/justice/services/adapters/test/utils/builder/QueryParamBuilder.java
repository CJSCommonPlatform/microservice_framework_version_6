package uk.gov.justice.services.adapters.test.utils.builder;

import org.raml.model.ParamType;
import org.raml.model.parameter.QueryParameter;

public class QueryParamBuilder {
    private boolean required;
    private String name;
    private ParamType type = ParamType.STRING;

    public static QueryParamBuilder queryParam() {
        return new QueryParamBuilder();
    }

    public static QueryParamBuilder queryParam(final String name) {
        return queryParam().withName(name);
    }

    public QueryParamBuilder required(final boolean required) {
        this.required = required;
        return this;
    }

    public QueryParamBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public QueryParamBuilder withType(final ParamType type) {
        this.type = type;
        return this;
    }

    public QueryParameter build() {
        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setDisplayName(name);
        queryParameter.setType(type);
        queryParameter.setRequired(required);
        return queryParameter;
    }
}
