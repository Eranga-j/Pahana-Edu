package com.pahanaedu.service.resources;
import com.pahanaedu.service.dao.CustomerDAO; import com.pahanaedu.service.model.Customer;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.*;
import java.util.List;
@Path("customers") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class CustomersResource {
    private final CustomerDAO dao = new CustomerDAO();
    @GET public List<Customer> list() throws Exception { return dao.findAll(); }
    @GET @Path("{id}") public Response get(@PathParam("id") int id) throws Exception { Customer c = dao.findById(id); return c==null? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(c).build(); }
    @POST public Response create(Customer c) throws Exception {
        if (c.getAccountNumber()==null || c.getAccountNumber().isBlank() || c.getName()==null || c.getName().isBlank())
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error","accountNumber and name required")).build();
        return Response.status(Response.Status.CREATED).entity(dao.create(c)).build();
    }
    @PUT @Path("{id}") public Response update(@PathParam("id") int id, Customer c) throws Exception { return dao.update(id,c)? Response.ok(java.util.Map.of("status","updated")).build(): Response.status(Response.Status.NOT_FOUND).build(); }
    @DELETE @Path("{id}") public Response delete(@PathParam("id") int id) throws Exception { return dao.delete(id)? Response.ok(java.util.Map.of("status","deleted")).build(): Response.status(Response.Status.NOT_FOUND).build(); }
}
