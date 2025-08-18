<%-- 
    Document   : create.jsp
    Purpose    : Create a new Customer
    Author     : You
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Create Customer</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>

<h2>Create Customer</h2>

<c:if test="${not empty error}">
    <div style="color:red; font-weight:bold; margin-bottom:10px;">
        ${error}
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/customers" method="post">
    <input type="hidden" name="action" value="create"/>

    <div>
        <label>Account Number:</label><br/>
        <input type="text" name="accountNumber"
               value="${form_accountNumber != null ? form_accountNumber : ''}" required/>
    </div>

    <div>
        <label>Name:</label><br/>
        <input type="text" name="name"
               value="${form_name != null ? form_name : ''}" required/>
    </div>

    <div>
        <label>Address:</label><br/>
        <input type="text" name="address"
               value="${form_address != null ? form_address : ''}"/>
    </div>

    <div>
        <label>Phone:</label><br/>
        <input type="text" name="phone"
               value="${form_phone != null ? form_phone : ''}" required/>
    </div>

    <div style="margin-top:10px;">
        <button type="submit">Save Customer</button>
        <a href="${pageContext.request.contextPath}/customers">Cancel</a>
    </div>
</form>

</body>
</html>
