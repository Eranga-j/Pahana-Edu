<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, java.math.BigDecimal, java.math.RoundingMode, java.text.DecimalFormat" %>
<%@ include file="/common/header.jspf" %>

<style>
  /* make anchor.btn look like the button */
  .btn { text-decoration: none; display:inline-block; }

  @media print{
    header, footer, .nav, .no-print, .btn, .btn-back { display:none !important; }
    body  { background:#fff; }
    main  { padding:0 !important; }
    .card { border:none !important; box-shadow:none !important; }
    @page { size:A4; margin:12mm; }
    table { font-size:12pt; }
  }
</style>

<div class="card no-print" style="margin-bottom:12px">
  <% if (isAdmin) { %>
    <a class="btn-back" href="${pageContext.request.contextPath}/admin/dashboard.jsp">
      <span class="icon" aria-hidden="true"></span> Back
    </a>
  <% } else { %>
    <a class="btn-back" href="${pageContext.request.contextPath}/cashier/dashboard.jsp">
      <span class="icon" aria-hidden="true"></span> Back
    </a>
  <% } %>
</div>

<%
  Map<String,Object> bill = (Map<String,Object>) request.getAttribute("bill");
  if (bill == null) {
%>
  <div class="card"><p>No bill to display.</p></div>
<%
  } else {
    String billNo       = (String) bill.get("billNo");
    String customerName = (String) bill.get("customerName");
    Number customerIdN  = (Number) bill.get("customerId");
    Integer customerId  = customerIdN == null ? null : customerIdN.intValue();
    String createdAt    = (String) bill.get("createdAt");
    @SuppressWarnings("unchecked")
    List<Map<String,Object>> lines = (List<Map<String,Object>>) bill.get("items");

    DecimalFormat money = new DecimalFormat("#,##0.00");

    BigDecimal subtotal = BigDecimal.ZERO;
    if (lines != null) {
      for (Map<String,Object> l : lines) {
        BigDecimal up = (BigDecimal) l.get("unitPrice");
        Number qn     = (Number) l.get("qty");
        int qty       = qn == null ? 0 : qn.intValue();
        BigDecimal lt = (BigDecimal) l.get("lineTotal");
        if (lt == null) lt = (up == null ? BigDecimal.ZERO : up).multiply(new BigDecimal(qty));
        subtotal = subtotal.add(lt);
      }
    }
    BigDecimal tax   = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
    BigDecimal grand = subtotal.add(tax);
%>

<div class="card">
  <h2>Invoice <%= billNo %></h2>
  <p><strong>Customer:</strong>
    <%= (customerName != null && !customerName.isEmpty()) ? customerName
         : ("#" + (customerId == null ? "" : customerId)) %>
  </p>
  <p><strong>Date:</strong> <%= createdAt == null ? "" : createdAt %></p>

  <table>
    <tr><th>Item</th><th>Qty</th><th>Unit Price</th><th>Line Total</th></tr>
    <%
      if (lines != null) {
        for (Map<String,Object> l : lines) {
          String itemName      = (String) l.get("itemName");
          Number qtyNum        = (Number) l.get("qty");
          int qty              = qtyNum == null ? 0 : qtyNum.intValue();
          BigDecimal unitPrice = (BigDecimal) l.get("unitPrice");
          BigDecimal lineTotal = (BigDecimal) l.get("lineTotal");
          if (lineTotal == null) lineTotal = (unitPrice == null ? BigDecimal.ZERO : unitPrice).multiply(new BigDecimal(qty));
          Number itemIdNum     = (Number) l.get("itemId");
    %>
      <tr>
        <td><%= (itemName != null && !itemName.isEmpty())
                 ? itemName
                 : (itemIdNum == null ? "Item" : "Item #" + itemIdNum.intValue()) %></td>
        <td><%= qty %></td>
        <td>LKR <%= unitPrice == null ? "0.00" : money.format(unitPrice) %></td>
        <td>LKR <%= money.format(lineTotal) %></td>
      </tr>
    <%
        }
      }
    %>
    <tr><th colspan="3" style="text-align:right">Subtotal</th>
        <th>LKR <%= money.format(subtotal) %></th></tr>
    <tr><th colspan="3" style="text-align:right">Tax (18%)</th>
        <th>LKR <%= money.format(tax) %></th></tr>
    <tr><th colspan="3" style="text-align:right">Grand Total</th>
        <th>LKR <%= money.format(grand) %></th></tr>
  </table>

  <!-- Actions -->
  <div class="no-print" style="margin-top:12px; display:flex; gap:8px; flex-wrap:wrap; align-items:center">

    <!-- SAVE: re-post current invoice back to /billing using the same fields BillingServlet expects -->
    <form id="saveForm" method="post" action="${pageContext.request.contextPath}/billing" style="display:inline">
      <input type="hidden" name="customerId" value="<%= customerId == null ? "" : customerId %>"/>
      <%
        if (lines != null) {
          for (Map<String,Object> l : lines) {
            Number itemIdNum = (Number) l.get("itemId");
            Integer itemId   = itemIdNum == null ? null : itemIdNum.intValue();
            Number qtyNum    = (Number) l.get("qty");
            int qty          = qtyNum == null ? 1 : qtyNum.intValue();
            BigDecimal up    = (BigDecimal) l.get("unitPrice");
            if (itemId != null) {
      %>
        <input type="hidden" name="itemId" value="<%= itemId %>"/>
        <input type="hidden" name="qty" value="<%= qty %>"/>
        <input type="hidden" name="unitPrice" value="<%= (up == null ? "0.00" : up) %>"/>
      <%
            }
          }
        }
      %>
      <button type="submit" class="btn" id="saveBtn">Save</button>
    </form>

    <button type="button" class="btn" onclick="window.print()">Print</button>
    <a class="btn" href="${pageContext.request.contextPath}/billing">New Bill</a>
  </div>
</div>

<script class="no-print">
  (function(){
    // Hide Save if this invoice already looks persisted (non TMP- bill number)
    var billNo = "<%= billNo == null ? "" : billNo %>";
    var saveBtn = document.getElementById('saveBtn');
    if (saveBtn && (!billNo || billNo.indexOf('TMP-') !== 0)) {
      saveBtn.style.display = 'none';
    }
  })();
</script>

<% } %>

<%@ include file="/common/footer.jspf" %>
