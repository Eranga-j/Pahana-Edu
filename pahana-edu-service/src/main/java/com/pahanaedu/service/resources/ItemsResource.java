package com.pahanaedu.service.resources;
import com.pahanaedu.service.dao.ItemDAO; import com.pahanaedu.service.model.Item;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.*; import java.util.List;
@Path("items") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class ItemsResource {
    private final ItemDAO dao = new ItemDAO();
    @GET public List<Item> list() throws Exception { return dao.findAll(); }
    @GET @Path("{id}") public Response get(@PathParam("id") int id) throws Exception { Item it = dao.findById(id); return it==null? Response.status(Response.Status.NOT_FOUND).build(): Response.ok(it).build(); }
    @POST public Response create(Item it) throws Exception {
        if (it.getSku()==null || it.getSku().isBlank() || it.getName()==null || it.getName().isBlank() || it.getUnitPrice()==null)
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error","sku, name, unitPrice required")).build();
        return Response.status(Response.Status.CREATED).entity(dao.create(it)).build();
    }
    @PUT @Path("{id}") public Response update(@PathParam("id") int id, Item it) throws Exception { return dao.update(id,it)? Response.ok(java.util.Map.of("status","updated")).build(): Response.status(Response.Status.NOT_FOUND).build(); }
    @DELETE @Path("{id}") public Response delete(@PathParam("id") int id) throws Exception { return dao.delete(id)? Response.ok(java.util.Map.of("status","deleted")).build(): Response.status(Response.Status.NOT_FOUND).build(); }
}
