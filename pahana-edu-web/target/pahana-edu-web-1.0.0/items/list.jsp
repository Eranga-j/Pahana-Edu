<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, java.text.DecimalFormat, java.math.BigDecimal" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/common/header.jspf" %>



<style>
  .btn.danger{ background:#dc2626; border-color:#dc2626; color:#fff; }
  .btn.danger:hover{ filter:brightness(0.96); }
  .btn.small{ padding:4px 8px; font-size:.9rem; }

  /* inline error banner (shown when servlet sets request attr "error") */
  .alert-error{
    background:#fef2f2; color:#991b1b;
    border:1px solid #fecaca; border-left:5px solid #dc2626;
    padding:12px 14px; border-radius:10px; margin-bottom:12px;
  }
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

<!-- If the servlet forwarded with an error (e.g., delete failed), show it -->
<c:if test="${not empty error}">
  <div class="alert-error">
    <strong>Action failed:</strong>
    <div><c:out value="${error}"/></div>
  </div>
</c:if>

<h2>Items</h2>
<div class="grid">

  <%-- Admin-only: Add/Edit form --%>
  <% if (isAdmin) { %>
  <div class="card">
    <h3>Add / Edit Item</h3>
    <form id="itemForm" method="post" action="${pageContext.request.contextPath}/items">
      <input type="hidden" name="id" id="itemId"/>
      <label>SKU</label><input name="sku" id="sku" required/>
      <label>Name</label><input name="name" id="name" required/>
      <label>Unit Price (LKR)</label><input name="unitPrice" id="unitPrice" type="number" step="0.01" min="0" required/>
      <div style="display:flex;gap:8px;margin-top:8px">
        <button class="btn" name="action" value="create">Save</button>
        <button class="btn secondary" name="action" value="update">Update</button>
        <button class="btn secondary" type="button" onclick="clearItemForm()">Clear</button>
      </div>
    </form>
  </div>
  <% } %>

  <div class="card">
    <h3>All Items</h3>
    <table>
      <tr>
        <th>ID</th><th>SKU</th><th>Name</th><th>Unit Price</th>
        <% if (isAdmin) { %><th style="width:220px">Actions</th><% } %>
      </tr>
      <%
        List<Map<String,Object>> arr =
          (List<Map<String,Object>>) request.getAttribute("items");
        DecimalFormat money = new DecimalFormat("#,##0.00");
        if (arr != null) {
          for (Map<String,Object> it : arr) {
            Number idNum    = (Number) it.get("id");
            int idVal       = (idNum == null ? 0 : idNum.intValue());
            String sku      = (String) it.get("sku");
            String nm       = (String) it.get("name");
            BigDecimal price= (BigDecimal) it.get("unitPrice");
      %>
      <tr>
        <td class="item-id"><%= idVal %></td>
        <td class="item-sku"><%= sku %></td>
        <td class="item-name"><%= nm %></td>
        <td class="item-price">LKR <%= (price == null ? "0.00" : money.format(price)) %></td>

        <% if (isAdmin) { %>
          <!-- raw price for precise editing -->
          <td class="item-price-raw" style="display:none"><%= (price == null ? "0.00" : price) %></td>
          <td>
            <button type="button" class="btn secondary" onclick="editItemRow(this)">Edit</button>

            <!-- Stand-alone delete form; servlet recognizes action=delete -->
            <form class="inline"
      method="post"
      action="${pageContext.request.contextPath}/items"
      onsubmit="return showDeleteConfirm(this,'item')"
      style="display:inline">
  <input type="hidden" name="id" value="<%= idVal %>"/>
  <input type="hidden" name="action" value="delete"/>
  <button type="submit" class="btn danger small">Delete</button>
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
  function editItemRow(btn){
    const tr = btn.closest('tr');
    document.getElementById('itemId').value   = tr.querySelector('.item-id').textContent.trim();
    document.getElementById('sku').value      = tr.querySelector('.item-sku').textContent.trim();
    document.getElementById('name').value     = tr.querySelector('.item-name').textContent.trim();
    const raw = (tr.querySelector('.item-price-raw')?.textContent || '0.00').trim();
    document.getElementById('unitPrice').value = raw;
    document.getElementById('sku').focus();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function clearItemForm(){
    ['itemId','sku','name','unitPrice'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });
  }

  // Friendlier confirmation with the item name in the prompt
  function confirmDelete(form){
    const tr  = form.closest('tr');
    const nm  = tr.querySelector('.item-name')?.textContent.trim() || 'this item';
    return confirm('Are you sure you want to delete "' + nm + '"?');
  }
</script>
<% } %>

<%@ include file="/common/footer.jspf" %>
