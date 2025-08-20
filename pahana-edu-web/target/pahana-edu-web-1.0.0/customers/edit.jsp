<%-- Edit Customer (with inline field errors) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.ee/jsp/jstl/core" prefix="c" %>


<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Edit Customer</title>
  <c:set var="ctx" value="${pageContext.request.contextPath}" />
  <link rel="stylesheet" href="${ctx}/css/style.css"/>
  <style>
    .field{ margin:12px 0; }
    .field label{ display:block; font-weight:600; margin-bottom:6px; }
    .input{ width:360px; max-width:100%; padding:8px 10px; border:1px solid #d1d5db; border-radius:8px; }
    .input.invalid{ border-color:#ef4444; background:#fff1f2; }
    .actions{ margin-top:14px; display:flex; gap:10px; align-items:center; }

    /* speech-bubble error */
    .err-bubble{
      margin-top:6px; position:relative; padding:10px 12px;
      border:1px solid #ef4444; background:#fee2e2; color:#991b1b;
      border-radius:10px; font-size:14px;
    }
    .err-bubble::before{
      content:""; position:absolute; top:-8px; left:18px;
      border:8px solid transparent; border-bottom-color:#ef4444;
    }
    .err-bubble::after{
      content:""; position:absolute; top:-7px; left:18px;
      border:8px solid transparent; border-bottom-color:#fee2e2;
    }
  </style>
</head>
<body>

<h2>Edit Customer</h2>

<%-- When servlet forwards with a general error message --%>
<c:if test="${not empty error}">
  <div class="err-bubble" role="alert">${error}</div>
</c:if>

<%-- ID can come from 'id' attribute or 'customer.id' --%>
<c:set var="cid" value="${empty id ? (empty customer ? '' : customer.id) : id}" />

<form action="${ctx}/customers" method="post" novalidate>
  <input type="hidden" name="action" value="update"/>
  <input type="hidden" name="id" value="${cid}"/>

  <!-- Account Number -->
  <div class="field">
    <label for="accountNumber">Account Number</label>
    <input id="accountNumber" name="accountNumber" required
           class="input ${not empty e_account ? 'invalid' : ''}"
           value="${not empty form_accountNumber ? form_accountNumber : (not empty customer.accountNumber ? customer.accountNumber : '')}"/>
    <c:if test="${not empty e_account}">
      <div class="err-bubble" role="alert">${e_account}</div>
    </c:if>
  </div>

  <!-- Name -->
  <div class="field">
    <label for="name">Name</label>
    <input id="name" name="name" required class="input"
           value="${not empty form_name ? form_name : (not empty customer.name ? customer.name : '')}"/>
  </div>

  <!-- Address -->
  <div class="field">
    <label for="address">Address</label>
    <input id="address" name="address" class="input"
           value="${not empty form_address ? form_address : (not empty customer.address ? customer.address : '')}"/>
  </div>

  <!-- Phone (Mobile) -->
  <div class="field">
    <label for="phone">Phone</label>
    <input id="phone" name="phone" required
           class="input ${not empty e_phone ? 'invalid' : ''}"
           value="${not empty form_phone ? form_phone : (not empty customer.phone ? customer.phone : '')}"/>
    <c:if test="${not empty e_phone}">
      <div class="err-bubble" role="alert">${e_phone}</div>
    </c:if>
  </div>

  <div class="actions">
    <button type="submit">Update Customer</button>
    <a href="${ctx}/customers">Cancel</a>
  </div>
</form>

<script>
  // focus first invalid field if present
  (function(){
    var bad = document.querySelector('.input.invalid');
    if (bad) bad.focus();
  })();
</script>

</body>
</html>
