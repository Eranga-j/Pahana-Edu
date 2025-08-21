<%-- Create Customer (with inline bubble errors) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Create Customer</title>
  <c:set var="ctx" value="${pageContext.request.contextPath}" />
  <link rel="stylesheet" href="${ctx}/css/style.css"/>

  <style>
    .form { max-width: 520px; }
    .field{ margin:14px 0; }
    .field label{ display:block; margin-bottom:6px; font-weight:600; }
    .input{ width:100%; padding:10px 12px; border:1px solid #d1d5db; border-radius:8px; }
    .input.invalid{ border-color:#ef4444; background:#fff1f2; }
    .actions{ margin-top:14px; display:flex; gap:10px; align-items:center; }
    .alert-error{
      margin:12px 0; padding:10px 12px; border:1px solid #ef4444; background:#fee2e2; color:#991b1b; border-radius:10px;
      font-weight:600;
    }
    .err-bubble{
      margin-top:8px; position:relative; padding:10px 12px;
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

<h2>Create Customer</h2>

<c:if test="${not empty error}">
  <div class="alert-error">
    <c:out value="${error}" />
  </div>
</c:if>

<form class="form" action="${ctx}/customers" method="post" novalidate>
  <input type="hidden" name="action" value="create"/>

  <!-- Account Number -->
  <div class="field">
    <label for="accountNumber">Account Number</label>
    <input id="accountNumber" name="accountNumber" required
           class="input ${not empty e_account || not empty error ? 'invalid' : ''}"
           value="${form_accountNumber}"/>
    <c:if test="${not empty e_account}">
      <div class="err-bubble" role="alert"><c:out value="${e_account}" /></div>
    </c:if>
    <!-- If the servlet only sets a global 'error' (e.g. on duplicate), show it under this field as well -->
    <c:if test="${empty e_account and not empty error}">
      <div class="err-bubble" role="alert">Duplicate customer: this account number is already registered. Please use a new one.</div>
    </c:if>
  </div>

  <!-- Name -->
  <div class="field">
    <label for="name">Name</label>
    <input id="name" name="name" required class="input"
           value="${form_name}"/>
  </div>

  <!-- Address -->
  <div class="field">
    <label for="address">Address</label>
    <input id="address" name="address" class="input"
           value="${form_address}"/>
  </div>

  <!-- Phone -->
  <div class="field">
    <label for="phone">Phone</label>
    <input id="phone" name="phone" required
           class="input ${not empty e_phone ? 'invalid' : ''}"
           value="${form_phone}"/>
    <c:if test="${not empty e_phone}">
      <div class="err-bubble" role="alert"><c:out value="${e_phone}" /></div>
    </c:if>
  </div>

  <div class="actions">
    <button type="submit">Save Customer</button>
    <a href="${ctx}/customers">Cancel</a>
  </div>
</form>

<script>
  document.addEventListener('DOMContentLoaded', function(){
    var bad = document.querySelector('.input.invalid');
    if (bad) bad.focus();
  });
</script>

</body>
</html>
