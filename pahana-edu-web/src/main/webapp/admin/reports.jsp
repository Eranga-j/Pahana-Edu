<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"  %>
<%@ include file="/common/header.jspf" %>

<c:if test="${not empty error}">
  <div class="alert-error">${error}</div>
</c:if>


<!-- styles omitted for brevity -->

<h2>Sales Reports</h2>

<div class="card filters">
  <form method="get" action="${pageContext.request.contextPath}/admin/reports">
    <label>From</label>
    <input type="date" name="from" value="${from}">
    <label>To</label>
    <input type="date" name="to" value="${to}">
    <button class="btn">Run</button>
    <a class="btn secondary" href="${pageContext.request.contextPath}/admin/reports">Clear</a>
  </form>
</div>

<c:if test="${not empty error}">
  <div class="alert-error">
    <strong>Couldn’t load data:</strong>
    <div><c:out value="${error}"/></div>
  </div>
</c:if>

<div class="card">
  <table>
    <tr>
      <th>Bill No</th>
      <th>Date</th>
      <th>Customer</th>
      <th>Lines</th>
      <th>Total</th>
    </tr>

    <c:choose>
      <c:when test="${not empty bills}">
        <c:forEach items="${bills}" var="b">
          <tr>
            <td><c:out value="${b.billNo}"/></td>
            <td><c:out value="${b.createdAt}"/></td>
            <td>
              <c:choose>
                <c:when test="${not empty b.customerName}">
                  <c:out value="${b.customerName}"/>
                </c:when>
                <c:otherwise>
                  #<c:out value="${b.customerId}"/>
                </c:otherwise>
              </c:choose>
            </td>
            <td class="num"><c:out value="${b.lineCount}"/></td>
            <td class="num">
              <fmt:formatNumber value="${b.totalAmount}" type="number" minFractionDigits="2" />
            </td>
          </tr>
        </c:forEach>
        <tr>
          <th colspan="4" style="text-align:right">Grand Total</th>
          <th class="num">
            <fmt:formatNumber value="${sumAmount}" type="number" minFractionDigits="2" />
          </th>
        </tr>
      </c:when>
      <c:otherwise>
        <tr>
          <td colspan="5" style="text-align:center;color:#6b7280;padding:14px;">
            No data for the selected range.
          </td>
        </tr>
      </c:otherwise>
    </c:choose>
  </table>
</div>

<%@ include file="/common/footer.jspf" %>
