<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head><title>Customer Report</title></head>
<body>
<h2>Customer Sales Report</h2>
<form method="get">
  From: <input type="date" name="from" value="${param.from}"/>
  To: <input type="date" name="to" value="${param.to}"/>
  <button type="submit">Run</button>
</form>

<c:set var="api" value="${pageContext.request.contextPath}/api/reports/customers?from=${param.from}&to=${param.to}"/>

<script>
async function load(){
  try {
    const res = await fetch("${api}");
    if(!res.ok){ throw new Error("HTTP " + res.status); }
    const data = await res.json();
    const tbody = document.querySelector("#rows");
    tbody.innerHTML = "";
    if (!Array.isArray(data) || data.length === 0){
      tbody.innerHTML = '<tr><td colspan="5">No data for selected range.</td></tr>';
      return;
    }
    data.forEach(r => {
      const tr = document.createElement("tr");
      const fmt = (v) => (v==null ? "" : v);
      tr.innerHTML = `<td>${fmt(r.customerName)}</td>
                      <td>${fmt(r.invoices)}</td>
                      <td>${fmt(r.totalSpent)}</td>
                      <td>${fmt(r.firstPurchase)}</td>
                      <td>${fmt(r.lastPurchase)}</td>`;
      tbody.appendChild(tr);
    });
  } catch(err){
    const tbody = document.querySelector("#rows");
    tbody.innerHTML = `<tr><td colspan="5">Error: ${err.message}</td></tr>`;
  }
}
window.addEventListener('load', load);
</script>

<table border="1" cellpadding="4" cellspacing="0">
  <thead><tr><th>Customer</th><th>Invoices</th><th>Total Spent</th><th>First Purchase</th><th>Last Purchase</th></tr></thead>
  <tbody id="rows"><tr><td colspan="5">Loading…</td></tr></tbody>
</table>
</body>
</html>