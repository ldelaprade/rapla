package org.rapla.server.provider.resteasy;

import org.rapla.server.provider.ExceptionResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RestWebApplicationExceptionMapper  implements ExceptionMapper<WebApplicationException>
{
    HttpServletRequest request;

    public RestWebApplicationExceptionMapper(@Context HttpServletRequest request)
    {
        this.request = request;
    }

    @Override
    public Response toResponse(WebApplicationException container)
    {
        return ExceptionResponseBuilder.toResponse( container, request);
    }
}
