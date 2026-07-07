<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>OrderCo - New Order</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/style.css"/>
</head>
<body>
<div class="container">

<div class="topbar">
    <a class="brand" href="orders.do">OrderCo</a>
</div>

<h1>New Purchase Order</h1>

<form action="createOrder.do" method="post">
    <label for="customerId">Customer</label>
    <select name="customerId" id="customerId">
        <c:forEach var="customer" items="${customers}">
            <option value="${customer.id}">${customer.name}</option>
        </c:forEach>
    </select>

    <button type="submit">Create Order</button>
</form>

<p><a href="orders.do">&laquo; Back to order list</a></p>

</div>
</body>
</html>
