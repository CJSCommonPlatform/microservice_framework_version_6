package org.raml.test.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("pathA")
public interface PathAResource {
  @GET
  @Produces("application/vnd.ctx.query.defquery+json")
  Response getPathA();
}
