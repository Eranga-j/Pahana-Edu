<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List,java.util.Map,java.math.BigDecimal" %>
<%@ include file="/common/header.jspf" %>

<!-- Back -->
<div class="card" style="margin-bottom:12px">
  <% if (isAdmin) { %>
    <a class="btn-back" href="${pageContext.request.contextPath}/admin/dashboard.jsp"><span class="icon"></span> Back</a>
  <% } else { %>
    <a class="btn-back" href="${pageContext.request.contextPath}/cashier/dashboard.jsp"><span class="icon"></span> Back</a>
  <% } %>
</div>

<h2>Create Bill</h2>
<div class="card">
  <form method="post" action="${pageContext.request.contextPath}/billing" onsubmit="return beforeSubmit()">
    <!-- Customer -->
    <label>Customer</label>
    <select name="customerId" required>
      <option value="">-- Select --</option>
      <%
        List<Map<String,Object>> customers =
            (List<Map<String,Object>>) request.getAttribute("customers");
        if (customers != null) {
          for (Map<String,Object> c : customers) {
            Integer id = (Integer) c.get("id");
            String acc = (String)  c.get("accountNumber");
            String nm  = (String)  c.get("name");
      %>
        <option value="<%= id %>"><%= acc %> &mdash; <%= nm %></option>
      <%
          }
        }
      %>
    </select>

    <h3 style="margin-top:16px">Items</h3>

    <style>
      /* ===== Compact lines ===== */
      #lines{ display:flex; flex-direction:column; gap:8px; }

      /* Header row (titles only once) */
      .line.header{ border:0; padding:0; background:transparent; }
      .line.header .grid{
        display:grid; gap:8px;
        grid-template-columns: minmax(220px,2fr) 110px 160px 160px 40px;
        align-items:center; color:#374151; font-weight:600;
      }
      .line.header .grid > div:last-child{ text-align:right; }

      /* Each line row */
      .line{
        border:1px solid #e5e7eb; border-radius:10px; padding:8px; background:#fff;
      }
      .line .grid{
        display:grid; gap:8px;
        grid-template-columns: minmax(220px,2fr) 110px 160px 160px 40px;
        align-items:center;
      }

      .delete-cell{ display:flex; justify-content:flex-end; }
      .btn.danger{ background:#dc2626; border-color:#dc2626; color:#fff; }
      .btn.danger:hover{ filter:brightness(0.96); }

      .money{ font-variant-numeric: tabular-nums; }
      .line-total-box{
        padding:8px; border:1px solid #e5e7eb; border-radius:8px; background:#f9fafb;
      }

      /* Totals block */
      .totals{ margin-top:14px; width:100%; max-width:480px; }
      .totals table{ width:100%; border-collapse:collapse; }
      .totals td{ padding:8px 0; }
      .totals td:last-child{ text-align:right; }
      .totals .grand{ border-top:1px dashed #d1d5db; padding-top:10px; font-weight:800; }

      @media (max-width:640px){
        .line .grid, .line.header .grid{
          grid-template-columns: 1fr 1fr 1fr 1fr 40px;
        }
      }
    </style>

    <!-- Column titles (shown once) -->
    <div class="line header">
      <div class="grid">
        <div>Item</div>
        <div>Qty</div>
        <div>Unit Price</div>
        <div>Line Total</div>
        <div><!-- delete --></div>
      </div>
    </div>

    <!-- Lines container -->
    <div id="lines"></div>

    <button type="button" class="btn secondary" onclick="addLine()">+ Add Line</button>

    <!-- Live totals -->
    <div class="totals card">
      <table aria-label="Totals">
        <tr><td>Subtotal</td>   <td class="money" id="subtotal">LKR 0.00</td></tr>
        <tr><td>Tax (18%)</td>  <td class="money" id="tax">LKR 0.00</td></tr>
        <tr><td class="grand">Grand Total</td><td class="money grand" id="grand">LKR 0.00</td></tr>
      </table>
    </div>

    <!-- Hidden fields sent to the server -->
    <input type="hidden" name="calcSubtotal" id="hSubtotal"/>
    <input type="hidden" name="calcTax" id="hTax"/>
    <input type="hidden" name="calcGrand" id="hGrand"/>

    <div style="margin-top:12px"><button class="btn">Create Bill</button></div>
  </form>
</div>

<script>
  /* ---------- Data from server for item dropdown ---------- */
  const ITEMS = [
    <%
      List<Map<String,Object>> its = (List<Map<String,Object>>) request.getAttribute("items");
      if (its != null) {
        for (int i=0; i<its.size(); i++){
          Map<String,Object> it = its.get(i);
          Integer id = (Integer) it.get("id");
          String sku = (String) it.get("sku");
          String name= (String) it.get("name");
          BigDecimal price = (BigDecimal) it.get("unitPrice");
    %>
      { id:<%=id%>, sku:"<%=sku%>", name:"<%=name%>", price:"<%=price%>" }<%= (i<its.size()-1 ? "," : "") %>
    <%
        }
      }
    %>
  ];

  function optionsHtml(){
    var html = '<option value="">--</option>';
    for (var i = 0; i < ITEMS.length; i++) {
      var it = ITEMS[i];
      html += '<option value="' + it.id + '" data-price="' + it.price + '">'
           +  it.sku + ' &mdash; ' + it.name + '</option>';
    }
    return html;
  }

  /* ---------- Helpers ---------- */
  function to2(n){
    var num = +n; if (!isFinite(num)) num = 0;
    return Math.round(num * 100) / 100;
  }
  function money(n){ return 'LKR ' + to2(n).toFixed(2); }

  function lineTotalFor(row){
    var qtyEl   = row.querySelector('input[name="qty"]');
    var priceEl = row.querySelector('input[name="unitPrice"]');
    if (qtyEl.value === '' || +qtyEl.value < 1)    qtyEl.value = 1;
    if (priceEl.value === '' || +priceEl.value < 0) priceEl.value = 0;
    var qty   = to2(qtyEl.value);
    var price = to2(priceEl.value);
    var total = to2(qty * price);
    row.querySelector('.line-total').textContent = money(total);
    return total;
  }

  function recalcTotals(){
    var subtotal = 0;
    document.querySelectorAll('#lines .line').forEach(function(row){
      subtotal += lineTotalFor(row);
    });
    var tax   = to2(subtotal * 0.18);
    var grand = to2(subtotal + tax);
    document.getElementById('subtotal').textContent = money(subtotal);
    document.getElementById('tax').textContent      = money(tax);
    document.getElementById('grand').textContent    = money(grand);
    document.getElementById('hSubtotal').value = to2(subtotal).toFixed(2);
    document.getElementById('hTax').value      = to2(tax).toFixed(2);
    document.getElementById('hGrand').value    = to2(grand).toFixed(2);
  }

  /* ---------- Row actions (compact rows, no labels) ---------- */
  function addLine(){
    var div = document.createElement('div');
    div.className = 'line';
    var opts = optionsHtml();
    div.innerHTML =
      '<div class="grid">'
    + '  <div><select name="itemId" onchange="syncPrice(this)">' + opts + '</select></div>'
    + '  <div><input name="qty" type="number" min="1" value="1" /></div>'
    + '  <div><input name="unitPrice" type="number" step="0.01" min="0" /></div>'
    + '  <div><div class="line-total-box money"><span class="line-total">LKR 0.00</span></div></div>'
    + '  <div class="delete-cell"><button type="button" class="btn danger" onclick="removeLine(this)" title="Delete">✖</button></div>'
    + '</div>';

    document.getElementById('lines').appendChild(div);
    div.querySelector('input[name="qty"]').addEventListener('input', recalcTotals);
    div.querySelector('input[name="unitPrice"]').addEventListener('input', recalcTotals);
    recalcTotals();
  }

  function removeLine(btn){
    var container = document.getElementById('lines');
    var lines = container.querySelectorAll('.line');
    if (lines.length > 1) {
      btn.closest('.line').remove();
    } else {
      var row = lines[0];
      row.querySelector('select[name="itemId"]').selectedIndex = 0;
      row.querySelector('input[name="qty"]').value = 1;
      row.querySelector('input[name="unitPrice"]').value = '';
    }
    recalcTotals();
  }

  function syncPrice(sel){
    var price = sel.options[sel.selectedIndex]
              ? sel.options[sel.selectedIndex].getAttribute('data-price') : '';
    var priceInput = sel.closest('.line').querySelector('input[name="unitPrice"]');
    if (priceInput) priceInput.value = price || '';
    recalcTotals();
  }

  function beforeSubmit(){ recalcTotals(); return true; }

  // Start with one empty line & initial totals
  addLine();
</script>

<%@ include file="/common/footer.jspf" %>
