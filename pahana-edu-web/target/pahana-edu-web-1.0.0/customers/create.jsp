<%-- Create Customer (with inline bubble errors) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.ee/jsp/jstl/core" prefix="c" %>


<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Create Customer</title>
  <c:set var="ctx" value="${pageContext.request.contextPath}" />
  <link rel="stylesheet" href="${ctx}/css/style.css"/>
  

  <style>
    /* Minimal styles for bubble errors; keep or move to style.css */
    .form { max-width: 520px; }
    .field{ margin:14px 0; }
    .field label{ display:block; margin-bottom:6px; font-weight:600; }
    .input{ width:100%; padding:10px 12px; border:1px solid #d1d5db; border-radius:8px; }
    .input.invalid{ border-color:#ef4444; background:#fff1f2; }
    .actions{ margin-top:14px; display:flex; gap:10px; align-items:center; }
    /* speech-bubble error */
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

<c:if test="${not empty requestScope.error}">
  <div style="color:#b91c1c; margin:8px 0; font-weight:700;">
    ${requestScope.error}
  </div>
</c:if>

<form class="form" action="${ctx}/customers" method="post" novalidate>
  <input type="hidden" name="action" value="create"/>

  <c:if test="${not empty error}">
  <div class="alert-error">
    <strong>This Item Number is Already Registered. Please Try a new one</strong>
    
  </div>
</c:if>
  
  
  <!-- Account Number -->
  <div class="field">
    <label for="accountNumber">Account Number</label>
    <input id="accountNumber" name="accountNumber" required
           class="input ${not empty e_account ? 'invalid' : ''}"
           value="${form_accountNumber}"/>
    <c:if test="${not empty e_account}">
      <div class="err-bubble" role="alert">${e_account}</div>
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
      <div class="err-bubble" role="alert">${e_phone}</div>
    </c:if>
  </div>

  <div class="actions">
    <button type="submit">Save Customer</button>
    <a href="${ctx}/customers">Cancel</a>
  </div>
</form>

<script>
  // Focus the first invalid input if any
  document.addEventListener('DOMContentLoaded', function(){
    var bad = document.querySelector('.input.invalid');
    if (bad) bad.focus();
  });
</script>

</body>
</html>
