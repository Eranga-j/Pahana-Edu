package com.pahanaedu.service.resources;
import com.pahanaedu.service.dao.UserDAO; import com.pahanaedu.service.model.User;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.*;
@Path("auth") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    public static class LoginRequest { public String username; public String password; }
    @POST @Path("login")
    public Response login(LoginRequest req){
        if (req==null || req.username==null || req.username.isBlank() || req.password==null || req.password.isBlank())
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error","Username and password are required")).build();
        try{
            UserDAO dao = new UserDAO();
            String salt = dao.getSaltForUser(req.username);
            if (salt == null) return Response.status(Response.Status.UNAUTHORIZED).entity(java.util.Map.of("error","Invalid credentials")).build();
            String pwdHash = Hashing.sha256Hex(req.password + salt);
            User user = dao.validateLogin(req.username, pwdHash);
            if (user == null) return Response.status(Response.Status.UNAUTHORIZED).entity(java.util.Map.of("error","Invalid credentials")).build();
            return Response.ok(user).build();
        }catch(Exception e){ e.printStackTrace(); return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build(); }
    }
}
