<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>OrderCo - Order Not Found</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/style.css"/>
</head>
<body>
<div class="container">

<div class="topbar">
    <a class="brand" href="orders.do">OrderCo</a>
</div>

<h1>Order Not Found</h1>
<p class="error">${errorMessage}</p>
<p><a href="orders.do">&laquo; Back to order list</a></p>

</div>
</body>
</html>
