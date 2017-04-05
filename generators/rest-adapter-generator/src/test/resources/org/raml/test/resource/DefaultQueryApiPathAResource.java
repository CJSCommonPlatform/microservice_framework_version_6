package org.raml.test.resource;

import uk.gov.justice.services.adapter.rest.parameter.ParameterCollectionBuilder;
import uk.gov.justice.services.adapter.rest.parameter.ValidParameterCollectionBuilder;
import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Adapter(Component.COMMAND_API)
public class DefaultPathAResource implements PathAResource {
    @Inject
    RestProcessor restProcessor;

    @Context
    HttpHeaders headers;

    @Inject
    RestProcessor restProcessor;

    @Override
    public Response getPathA() {
        ParameterCollectionBuilder validParameterCollectionBuilder = new ValidParameterCollectionBuilder();
        return restProcessor.process("OkStatusEnvelopePayloadEntityResponseStrategy", interceptorContext -> Optional.empty(), "", Optional.empty(), headers, validParameterCollectionBuilder.parameters());
    }
}
