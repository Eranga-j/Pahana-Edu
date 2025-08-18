<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ include file="/common/header.jspf" %>



<style>
  .btn.danger{ background:#dc2626; border-color:#dc2626; color:#fff; }
  .btn.danger:hover{ filter:brightness(0.96); }
  .btn.small{ padding:4px 8px; font-size:.9rem; }
</style>

<!-- Back -->
<div class="card" style="margin-bottom:12px">
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

<h2>Customers</h2>
<div class="grid">
  <%-- Admin-only: Add/Edit form --%>
  <% if (isAdmin) { %>
  <div class="card">
    <h3>Add / Edit Customer</h3>
    <form id="custForm" method="post" action="${pageContext.request.contextPath}/customers">
      <input type="hidden" name="id" id="custId"/>
      <label>Account No</label><input name="accountNumber" id="accNo" required/>
      <label>Name</label><input name="name" id="name" required/>
      <label>Address</label><input name="address" id="address"/>
      <label>Phone</label><input name="phone" id="phone"/>
      <div style="display:flex;gap:8px;margin-top:8px">
        <button class="btn" name="action" value="create">Save</button>
        <button class="btn secondary" name="action" value="update">Update</button>
        <button class="btn secondary" type="button" onclick="clearForm()">Clear</button>
      </div>
    </form>
  </div>
  <% } %>

  <div class="card">
    <h3>All Customers</h3>
    <table>
      <tr>
        <th>ID</th><th>Account No</th><th>Name</th><th>Phone</th>
        <% if (isAdmin) { %><th style="width:220px">Actions</th><% } %>
      </tr>
      <%
        List<Map<String,Object>> arr =
            (List<Map<String,Object>>) request.getAttribute("customers");
        if (arr != null) {
          for (Map<String,Object> c : arr) {
            Number idNum = (Number) c.get("id");
            int idVal    = (idNum == null ? 0 : idNum.intValue());
            String acc   = (String) c.get("accountNumber");
            String nm    = (String) c.get("name");
            String ph    = (String) c.get("phone");
            String ad    = (String) c.get("address");
      %>
      <tr>
        <td class="cust-id"><%= idVal %></td>
        <td class="cust-acc"><%= acc %></td>
        <td class="cust-name"><%= nm %></td>
        <td class="cust-phone"><%= ph == null ? "" : ph %></td>

        <% if (isAdmin) { %>
          <!-- keep address hidden in the row so Edit can fill it -->
          <td class="cust-address" style="display:none"><%= ad == null ? "" : ad %></td>
          <td>
            <button type="button" class="btn secondary" onclick="editRow(this)">Edit</button>

            <!-- Stand-alone delete form -->
           <form class="inline"
      method="post"
      action="${pageContext.request.contextPath}/customers"
      onsubmit="return showDeleteConfirm(this,'customer')"
      style="display:inline">
  <input type="hidden" name="id" value="<%= idNum == null ? "" : idNum.intValue() %>"/>
  <input type="hidden" name="action" value="delete"/>
  <button class="btn danger small" type="submit">Delete</button>
</form>

              
              
          </td>
        <% } %>
      </tr>
      <%
          }
        }
      %>
    </table>
  </div>
</div>

<% if (isAdmin) { %>
<script>
  function editRow(btn){
    const tr = btn.closest('tr');
    document.getElementById('custId').value = tr.querySelector('.cust-id').textContent.trim();
    document.getElementById('accNo').value  = tr.querySelector('.cust-acc').textContent.trim();
    document.getElementById('name').value   = tr.querySelector('.cust-name').textContent.trim();
    document.getElementById('phone').value  = tr.querySelector('.cust-phone').textContent.trim();
    const addrCell = tr.querySelector('.cust-address');
    document.getElementById('address').value = addrCell ? addrCell.textContent.trim() : '';
    document.getElementById('accNo').focus();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  function clearForm(){
    ['custId','accNo','name','address','phone'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });
  }
</script>
<% } %>

<%@ include file="/common/footer.jspf" %>
