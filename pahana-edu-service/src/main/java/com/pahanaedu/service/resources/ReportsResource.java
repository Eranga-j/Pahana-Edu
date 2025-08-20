package com.pahanaedu.service.resources;

import com.pahanaedu.service.dao.ReportDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.*;

@Path("reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportsResource {
    private final ReportDAO dao = new ReportDAO();

    private LocalDate parse(String s) { return (s == null || s.isBlank()) ? null : LocalDate.parse(s); }

    @GET
    @Path("sales")
    public List<ReportDAO.SalesSummaryRow> sales(@QueryParam("from") String from, @QueryParam("to") String to) throws Exception {
        return dao.getSalesSummary(parse(from), parse(to));
    }

    @GET
    @Path("sales-by-item")
    public List<ReportDAO.ItemSalesRow> salesByItem(@QueryParam("from") String from, @QueryParam("to") String to) throws Exception {
        return dao.getSalesByItem(parse(from), parse(to));
    }

    @GET
    @Path("customers")
    public List<ReportDAO.CustomerSalesRow> customers(@QueryParam("from") String from, @QueryParam("to") String to) throws Exception {
        return dao.getSalesByCustomer(parse(from), parse(to));
    }
}