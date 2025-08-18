<%-- 
    Document   : edit.jsp
    Purpose    : Edit an existing Customer
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit Customer</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
    <style>
        .form-row{ margin:8px 0; }
        .form-row input{ width:320px; max-width:100%; }
        .actions{ margin-top:12px; display:flex; gap:8px; align-items:center; }
        .alert{ color:#b91c1c; background:#fee2e2; border:1px solid #fecaca; padding:8px 10px; border-radius:6px; }
        label{ display:block; font-weight:600; margin-bottom:4px; }
    </style>
</head>
<body>

<h2>Edit Customer</h2>

<c:if test="${not empty error}">
  <div class="alert">${error}</div>
</c:if>

<%-- Expect one of:
     A) servlet set: id + form_* (after failed submit)
     B) servlet set: customer map with keys id, accountNumber, name, address, phone
--%>

<c:set var="cid" value="${empty id ? (empty customer ? '' : customer.id) : id}" />

<form action="${pageContext.request.contextPath}/customers" method="post">
    <input type="hidden" name="action" value="update"/>
    <input type="hidden" name="id" value="${cid}"/>

    <div class="form-row">
        <label>Account Number</label>
        <input type="text" name="accountNumber"
               value="${form_accountNumber != null ? form_accountNumber : (customer.accountNumber != null ? customer.accountNumber : '')}"
               required/>
    </div>

    <div class="form-row">
        <label>Name</label>
        <input type="text" name="name"
               value="${form_name != null ? form_name : (customer.name != null ? customer.name : '')}"
               required/>
    </div>

    <div class="form-row">
        <label>Address</label>
        <input type="text" name="address"
               value="${form_address != null ? form_address : (customer.address != null ? customer.address : '')}"/>
    </div>

    <div class="form-row">
        <label>Phone</label>
        <input type="text" name="phone"
               value="${form_phone != null ? form_phone : (customer.phone != null ? customer.phone : '')}"
               required/>
    </div>

    <div class="actions">
        <button type="submit">Update Customer</button>
        <a href="${pageContext.request.contextPath}/customers">Cancel</a>
    </div>
</form>

</body>
</html>
