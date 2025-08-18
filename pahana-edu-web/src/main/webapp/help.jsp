<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/common/header.jspf" %>

<h2>Help</h2>
<div class="card">
  <ol>
    <li>Login with your username and password.</li>
    <li>Admins can manage customers and items (CRUD) via the respective pages.</li>
    <li>Cashiers can create bills and view customers.</li>
    <li>Use the Billing page to add line items and create invoices, then click <em>Print</em>.</li>
  </ol>
  <p>
    This system uses a 3-tier architecture (DAO + Service + REST) with MVC on the web client.
    Sessions and role checks secure the Admin and Cashier panels.
  </p>
</div>

<%@ include file="/common/footer.jspf" %>
