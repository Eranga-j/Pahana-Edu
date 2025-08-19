package com.pahanaedu.web.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "BillPdfServlet", urlPatterns = {"/bills/pdf"})
public class BillPdfServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        @SuppressWarnings("unchecked")
        Map<String, Object> bill =
                (Map<String, Object>) req.getSession().getAttribute("billForPrint");

        if (bill == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No bill to print. Create/view a bill first.");
            return;
        }

        String billNo       = String.valueOf(bill.getOrDefault("billNo", "BILL"));
        String customerName = String.valueOf(bill.getOrDefault("customerName", "Walk-in Customer"));
        String createdAt    = String.valueOf(bill.getOrDefault("createdAt", ""));

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + billNo + ".pdf\"");

        try (OutputStream out = resp.getOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font H1  = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font H2  = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font TXT = new Font(Font.HELVETICA, 10, Font.NORMAL);

            Paragraph title = new Paragraph("Invoice", H1);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" ", TXT));

            Paragraph meta = new Paragraph(
                    "Bill No : " + billNo + "\n" +
                    "Date    : " + createdAt + "\n" +
                    "Customer: " + (customerName == null || customerName.isBlank()
                                   ? "Walk-in Customer" : customerName),
                    TXT
            );
            doc.add(meta);
            doc.add(new Paragraph(" ", TXT));

            // Items table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{52f, 12f, 18f, 18f});

            addHeader(table, "Item", H2);
            addHeader(table, "Qty", H2);
            addHeader(table, "Unit Price", H2);
            addHeader(table, "Line Total", H2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) bill.get("items");

            BigDecimal subtotal = BigDecimal.ZERO;

            if (items != null) {
                for (Map<String, Object> it : items) {
                    String itemName = String.valueOf(it.getOrDefault("itemName", ""));
                    int qty         = toInt(it.get("qty"), 1);
                    BigDecimal unit = toBD(it.get("unitPrice"));
                    BigDecimal line = toBD(it.getOrDefault("lineTotal", unit.multiply(BigDecimal.valueOf(qty))));

                    table.addCell(new Phrase(itemName, TXT));
                    table.addCell(new Phrase(String.valueOf(qty), TXT));
                    table.addCell(new Phrase(money(unit), TXT));
                    table.addCell(new Phrase(money(line), TXT));

                    subtotal = subtotal.add(line);
                }
            }

            doc.add(table);
            doc.add(new Paragraph(" ", TXT));

            BigDecimal tax   = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grand = subtotal.add(tax);

            Paragraph pSubtotal = new Paragraph("Subtotal  : " + money(subtotal), TXT);
            pSubtotal.setAlignment(Paragraph.ALIGN_RIGHT);
            doc.add(pSubtotal);

            Paragraph pTax = new Paragraph("Tax (18%) : " + money(tax), TXT);
            pTax.setAlignment(Paragraph.ALIGN_RIGHT);
            doc.add(pTax);

            Paragraph pGrand = new Paragraph("Grand Total: " + money(grand), H2);
            pGrand.setAlignment(Paragraph.ALIGN_RIGHT);
            doc.add(pGrand);

            doc.close();
        } catch (DocumentException e) {
            throw new ServletException(e);
        }
    }

    private static void addHeader(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(6f);
        t.addCell(c);
    }

    private static BigDecimal toBD(Object o) {
        if (o == null) return BigDecimal.ZERO;
        try {
            if (o instanceof BigDecimal b) return b;
            return new BigDecimal(o.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static int toInt(Object o, int def) {
        try { return Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return def; }
    }

    private static String money(BigDecimal v) {
        // Simple LKR prefix with 2 decimals
        return "LKR " + (v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP)).toPlainString();
    }
}
