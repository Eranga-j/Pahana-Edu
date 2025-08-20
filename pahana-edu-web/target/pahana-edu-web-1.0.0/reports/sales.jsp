<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>


<html>
<head><title>Sales Report</title></head>
<body>
<h2>Sales Report</h2>
<form method="get">
  From: <input type="date" name="from" value="${param.from}"/>
  To: <input type="date" name="to" value="${param.to}"/>
  <button type="submit">Run</button>
</form>
<c:set var="api" value="${pageContext.request.contextPath}/api/reports/sales?from=${param.from}&to=${param.to}"/>
<script>
async function load(){
  const res = await fetch("${api}");
  const data = await res.json();
  const tbody = document.querySelector("#rows");
  tbody.innerHTML = "";
  let total = 0;
  data.forEach(r => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${r.day}</td><td>${r.invoices}</td><td>${r.subtotal}</td><td>${r.discountAmount}</td><td>${r.taxAmount}</td><td>${r.totalAmount}</td>`;
    tbody.appendChild(tr);
    total += Number(r.totalAmount || 0);
  });
  document.querySelector("#grand").textContent = total.toFixed(2);
}
window.addEventListener('load', load);
</script>
<table border="1" cellpadding="4" cellspacing="0">
  <thead><tr><th>Day</th><th>Invoices</th><th>Subtotal</th><th>Discount</th><th>Tax</th><th>Total</th></tr></thead>
  <tbody id="rows"><tr><td colspan="6">Loading…</td></tr></tbody>
  <tfoot><tr><th colspan="5" style="text-align:right">Grand Total</th><th id="grand"></th></tr></tfoot>
</table>
</body>
</html>