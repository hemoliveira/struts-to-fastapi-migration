<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US"/>
<!DOCTYPE html>
<html>
<head>
    <title>OrderCo - Purchase Orders</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/style.css"/>
</head>
<body>
<div class="container">

<div class="topbar">
    <a class="brand" href="orders.do">OrderCo</a>
</div>

<%
    java.util.List<com.orderco.legacy.model.PurchaseOrder> ordersList = 
        (java.util.List<com.orderco.legacy.model.PurchaseOrder>) request.getAttribute("orders");
    java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
    java.math.BigDecimal sumAllTotals = java.math.BigDecimal.ZERO;
    int approvedCount = 0;
    int rejectedCount = 0;
    int pendingCount = 0;
    java.math.BigDecimal aov = java.math.BigDecimal.ZERO;
    double approvalRate = 0.0;

    if (ordersList != null && !ordersList.isEmpty()) {
        for (com.orderco.legacy.model.PurchaseOrder order : ordersList) {
            java.math.BigDecimal total = order.getTotal() != null ? order.getTotal() : java.math.BigDecimal.ZERO;
            sumAllTotals = sumAllTotals.add(total);
            
            if ("APPROVED".equals(order.getStatus())) {
                totalRevenue = totalRevenue.add(total);
                approvedCount++;
            } else if ("REJECTED".equals(order.getStatus())) {
                rejectedCount++;
            } else if ("SUBMITTED".equals(order.getStatus())) {
                pendingCount++;
            }
        }
        int totalFinished = approvedCount + rejectedCount;
        approvalRate = totalFinished > 0 ? ((double) approvedCount / totalFinished) * 100.0 : 0.0;
        aov = sumAllTotals.divide(new java.math.BigDecimal(ordersList.size()), 2, java.math.RoundingMode.HALF_UP);
    }
    pageContext.setAttribute("totalRevenue", totalRevenue);
    pageContext.setAttribute("aov", aov);
    pageContext.setAttribute("approvalRate", approvalRate);
    pageContext.setAttribute("pendingCount", pendingCount);
%>

<h1>Purchase Orders</h1>

<div class="dashboard-grid">
    <div class="metric-card">
        <span class="metric-label">💰 Total Revenue</span>
        <span class="metric-value">$<fmt:formatNumber value="${totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></span>
    </div>
    <div class="metric-card">
        <span class="metric-label">📊 Average Order (AOV)</span>
        <span class="metric-value">$<fmt:formatNumber value="${aov}" minFractionDigits="2" maxFractionDigits="2"/></span>
    </div>
    <div class="metric-card">
        <span class="metric-label">✅ Approval Rate</span>
        <span class="metric-value"><fmt:formatNumber value="${approvalRate}" minFractionDigits="1" maxFractionDigits="1"/>%</span>
    </div>
    <div class="metric-card">
        <span class="metric-label">⏳ Pending Approval</span>
        <span class="metric-value">${pendingCount}</span>
    </div>
</div>

<p style="margin: 1.5rem 0;">
    <a href="newOrder.do" style="background: var(--accent); color: #fff; padding: 0.55rem 1.1rem; border-radius: 6px; font-weight: 600; display: inline-block; text-decoration: none;">+ New Order</a>
</p>

<table class="grid">
    <thead>
    <tr>
        <th>ID</th>
        <th>Customer</th>
        <th>Status</th>
        <th>Created</th>
        <th>Total</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="order" items="${orders}">
        <tr>
            <td>${order.id}</td>
            <td>${order.customerName}</td>
            <td><span class="status status-${order.status}">${order.status}</span></td>
            <td><fmt:formatDate value="${order.createdDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td>$<fmt:formatNumber value="${order.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
            <td><a href="viewOrder.do?id=${order.id}" style="font-weight: 600;">View</a></td>
        </tr>
    </c:forEach>
    <c:if test="${empty orders}">
        <tr>
            <td colspan="6">No orders yet.</td>
        </tr>
    </c:if>
    </tbody>
</table>

</div>
</body>
</html>
