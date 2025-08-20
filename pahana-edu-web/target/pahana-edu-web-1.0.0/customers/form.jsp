<%@ taglib uri="http://jakarta.ee/jsp/jstl/core" prefix="c" %>

<style>
  .field{ margin:12px 0; }
  .field label{ display:block; margin-bottom:6px; font-weight:600; }
  .input{ width:100%; max-width:360px; padding:8px 10px; border:1px solid #d1d5db; border-radius:8px; }
  .input.invalid{ border-color:#ef4444; background:#fff1f2; }
  /* bubble error with notch */
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


<c:if test="${not empty requestScope.errorPhone}">
  <div style="color:#b91c1c; margin:8px 0; font-weight:700;">
    ${requestScope.errorPhone}
  </div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/customers">
  <input type="hidden" name="action" value="${formMode == null ? 'create' : formMode}">
  <c:if test="${not empty formId}">
    <input type="hidden" name="id" value="${formId}">
  </c:if>

  Account Number:
  <input name="accountNumber" value="${formAccountNumber}"><br>
  Name:
  <input name="name" value="${formName}"><br>
  Address:
  <input name="address" value="${formAddress}"><br>
  Phone:
  <input name="phone" value="${formPhone}"><br>

  <button type="submit">Save Customer</button>
  <a href="${pageContext.request.contextPath}/customers">Cancel</a>
</form>
