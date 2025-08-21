package com.pahanaedu.service.resources;

import com.pahanaedu.service.dao.CustomerDAO;
import com.pahanaedu.service.model.Customer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Path("customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomersResource {

    private final CustomerDAO dao = new CustomerDAO();

    @GET
    public Response list() {
        try {
            List<Customer> all = dao.findAll();
            return Response.ok(all).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "SERVER_ERROR", "message", "Unable to list customers"))
                    .build();
        }
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") int id) {
        try {
            Customer c = dao.findById(id);
            return (c == null) ? Response.status(Response.Status.NOT_FOUND).build()
                               : Response.ok(c).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "SERVER_ERROR", "message", "Unable to fetch customer"))
                    .build();
        }
    }

    @POST
    public Response create(Customer c) {
        // Basic validation
        if (c == null ||
            c.getAccountNumber() == null || c.getAccountNumber().isBlank() ||
            c.getName() == null || c.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "VALIDATION",
                                   "message", "accountNumber and name are required"))
                    .build();
        }

        try {
            Customer created = dao.create(c);
            return Response.status(Response.Status.CREATED).entity(created).build();

        } catch (SQLIntegrityConstraintViolationException dup) {
            // Unique constraint (account_number) violation --> 409 Conflict
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of(
                            "error", "DUPLICATE_ACCOUNT",
                            "field", "accountNumber",
                            "accountNumber", c.getAccountNumber(),
                            "message", "A customer with this account number already exists."
                    ))
                    .build();

        } catch (SQLException e) {
            return Response.serverError()
                    .entity(Map.of("error", "SERVER_ERROR", "message", "Could not save customer"))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "SERVER_ERROR", "message", "Unexpected error"))
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") int id, Customer c) {
        try {
            boolean ok = dao.update(id, c);
            return ok ? Response.ok(Map.of("status", "updated")).build()
                      : Response.status(Response.Status.NOT_FOUND).build();
        } catch (SQLIntegrityConstraintViolationException dup) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of(
                            "error", "DUPLICATE_ACCOUNT",
                            "field", "accountNumber",
                            "accountNumber", c != null ? c.getAccountNumber() : null,
                            "message", "A customer with this account number already exists."
                    ))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "SERVER_ERROR", "message", "Could not update customer"))
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") int id) {
        try {
            boolean ok = dao.delete(id);
            return ok ? Response.ok(Map.of("status", "deleted")).build()
                      : Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "SERVER_ERROR", "message", "Could not delete customer"))
                    .build();
        }
    }
}
