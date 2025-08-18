<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,java.math.BigDecimal,java.text.DecimalFormat" %>
<%@ include file="/common/header.jspf" %>

<style>
  .form-grid { display:grid; gap:12px; grid-template-columns: repeat(auto-fit,minmax(220px,1fr)); }
  .totals { margin-top:14px; width:100%; max-width:520px; }
  .totals table { width:100%; border-collapse:collapse; }
  .totals td { padding:8px 0; }
  .totals td:last-child { text-align:right; }
  .totals .grand { border-top:1px dashed #d1d5db; padding-top:10px; font-weight:800; }
  .subtle { color:#6b7280; font-size:0.95rem; }
</style>

<div class="card" style="margin-bottom:12px">
  <a class="btn-back" href="${pageContext.request.contextPath}/admin/dashboard.jsp">
    <span class="icon" aria-hidden="true"></span> Back
  </a>
</div>

<h2>Customer Bill Report</h2>

<div class="card">
    
  <form method="get" action="${pageContext.request.contextPath}/admin/customer-report">
    <div class="form-grid">
      <!-- Customer -->
      <div>
        <label>Customer</label>
        <select name="customerId" required>
          <option value="">-- Select --</option>
          <%
            List<Map<String,Object>> customers =
                (List<Map<String,Object>>) request.getAttribute("customers");
            String selectedId = (String) request.getAttribute("selectedCustomerId");
            if (customers != null) {
              for (Map<String,Object> c : customers) {
                Integer id = (Integer) c.get("id");
                String acc = (String) c.get("accountNumber");
                String nm  = (String) c.get("name");
                String sel = (selectedId != null && selectedId.equals(String.valueOf(id))) ? "selected" : "";
          %>
            <option value="<%= id %>" <%= sel %>><%= acc %> — <%= nm %></option>
          <%
              }
            }
          %>
        </select>
      </div>

      <!-- From / To (optional) -->
      <%
        String from = (String) request.getAttribute("from");
        String to   = (String) request.getAttribute("to");
      %>
      <div>
        <label>From </label>
        <input type="date" name="from" value="<%= from == null ? "" : from %>">
      </div>
      <div>
        <label>To</label>
        <input type="date" name="to" value="<%= to == null ? "" : to %>">
      </div>
    </div>

    <div class="subtle" style="margin-top:6px">
      Leave dates empty to see all bills for the selected customer. If you select only one date,
      it will be treated as a single-day range.
    </div>

    <div style="margin-top:12px">
      <button class="btn">Run</button>
      <a class="btn secondary" href="${pageContext.request.contextPath}/admin/customer-report">Clear</a>
    </div>
  </form>
</div>

<%
  String error = (String) request.getAttribute("error");
  if (error != null && !error.isEmpty()) {
%>
  <div class="card" style="border-color:#fecaca;background:#fef2f2;color:#991b1b">
    <strong>Error:</strong> <%= error %>
  </div>
<% } %>

<div class="card">
  <h3>Results</h3>
  <%
    List<Map<String,Object>> rows = (List<Map<String,Object>>) request.getAttribute("rows");
    BigDecimal sumAmount = (BigDecimal) request.getAttribute("sumAmount");
    DecimalFormat money = new DecimalFormat("#,##0.00");
    if (rows == null || rows.isEmpty()) {
  %>
    <p class="subtle">No data for the selected criteria.</p>
  <%
    } else {
  %>
    <table>
      <tr>
        <th>Bill No</th>
        <th>Date</th>
        <th>Items</th>
        <th style="width:160px">Total</th>
      </tr>
      <%
        for (Map<String,Object> b : rows) {
          String billNo = (String) b.get("billNo");
          String createdAt = (String) b.get("createdAt");
          BigDecimal total = (BigDecimal) b.get("totalAmount");
          List<Map<String,Object>> its = (List<Map<String,Object>>) b.get("items");
      %>
      <tr>
        <td><%= billNo %></td>
        <td><%= createdAt == null ? "" : createdAt %></td>
        <td>
          <ul style="margin:0;padding-left:18px">
            <%
              if (its != null) {
                for (Map<String,Object> l : its) {
                  String nm  = (String) l.get("itemName");
                  Number qn  = (Number) l.get("qty");
                  BigDecimal up = (BigDecimal) l.get("unitPrice");
                  BigDecimal lt = (BigDecimal) l.get("lineTotal");
            %>
              <li>
                <%= (nm == null ? "Item" : nm) %> × <%= (qn == null ? 0 : qn.intValue()) %>
                (<%= up == null ? "LKR 0.00" : "LKR " + money.format(up) %>)
                = <strong><%= lt == null ? "LKR 0.00" : "LKR " + money.format(lt) %></strong>
              </li>
            <%
                }
              }
            %>
          </ul>
        </td>
        <td><strong>LKR <%= total == null ? "0.00" : money.format(total) %></strong></td>
      </tr>
      <%
        }
      %>
    </table>

    <div class="totals">
      <table>
        <tr>
          <td class="grand">Grand Total</td>
          <td class="grand">LKR <%= sumAmount == null ? "0.00" : money.format(sumAmount) %></td>
        </tr>
      </table>
    </div>
  <%
    }
  %>
</div>

<%@ include file="/common/footer.jspf" %>
