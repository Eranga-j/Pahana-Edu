package com.pahanaedu.service.resources;
import com.pahanaedu.service.dao.BillDAO; import com.pahanaedu.service.model.*;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.*; import java.math.BigDecimal; import java.time.LocalDateTime; import java.sql.Timestamp; import java.util.*;
@Path("bills") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class BillsResource {
    private final BillDAO dao = new BillDAO();
    public static class CreateBillRequest { public int customerId; public int createdBy; public java.util.List<Line> lines; }
    public static class Line { public int itemId; public int qty; public java.math.BigDecimal unitPrice; }
    @POST public Response create(CreateBillRequest req){
        try{
            if (req==null || req.customerId<=0 || req.createdBy<=0 || req.lines==null || req.lines.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error","Invalid bill data")).build();
            java.util.List<BillItem> items = new java.util.ArrayList<>(); BigDecimal total = BigDecimal.ZERO;
            for (Line l: req.lines){ BigDecimal lineTotal = l.unitPrice.multiply(java.math.BigDecimal.valueOf(l.qty));
                items.add(new BillItem(0,l.itemId,null,l.qty,l.unitPrice,lineTotal)); total = total.add(lineTotal); }
            Bill bill = new Bill(); bill.setBillNo("INV-" + System.currentTimeMillis()); bill.setCustomerId(req.customerId); bill.setTotalAmount(total); bill.setCreatedBy(req.createdBy); bill.setItems(items);
            return Response.status(Response.Status.CREATED).entity(dao.create(bill)).build();
        }catch(Exception e){ e.printStackTrace(); return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build(); }
    }
    @GET @Path("{id}") public Response get(@PathParam("id") int id){
        try{ Bill b = dao.findById(id); return b==null? Response.status(Response.Status.NOT_FOUND).build(): Response.ok(b).build(); }
        catch(Exception e){ return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build(); }
    }
    @GET public Response list(@QueryParam("from") String from, @QueryParam("to") String to){
        try{
            java.time.LocalDateTime fromDt = (from==null || from.isBlank())? LocalDateTime.now().minusDays(30) : LocalDateTime.parse(from);
            java.time.LocalDateTime toDt = (to==null || to.isBlank())? LocalDateTime.now() : LocalDateTime.parse(to);
            return Response.ok(dao.listRange(Timestamp.valueOf(fromDt), Timestamp.valueOf(toDt))).build();
        }catch(Exception e){ return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build(); }
    }
}
