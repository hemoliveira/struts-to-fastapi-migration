<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US"/>
<!DOCTYPE html>
<html>
<head>
    <title>OrderCo - Order #${order.id}</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/style.css"/>
</head>
<body>
<div class="container">

<div class="topbar">
    <a class="brand" href="orders.do">OrderCo</a>
</div>

<h1>Order #${order.id}</h1>

<c:if test="${not empty errorMessage}">
    <p class="error">${errorMessage}</p>
</c:if>

<c:set var="isDraft" value="${order.status == 'DRAFT'}"/>
<c:set var="isSubmitted" value="${order.status == 'SUBMITTED'}"/>
<c:set var="isApproved" value="${order.status == 'APPROVED'}"/>
<c:set var="isRejected" value="${order.status == 'REJECTED'}"/>
<c:set var="isDecided" value="${isApproved || isRejected}"/>

<div class="stepper">
    <div class="step ${isDraft ? 'is-current' : 'is-complete'}">
        <span class="step-dot"><c:choose><c:when test="${isDraft}">1</c:when><c:otherwise>&#10003;</c:otherwise></c:choose></span>
        <span class="step-label">Draft</span>
    </div>
    <div class="step-line ${isDraft ? '' : 'is-complete'}"></div>
    <div class="step ${isSubmitted ? 'is-current' : (isDecided ? 'is-complete' : '')}">
        <span class="step-dot"><c:choose><c:when test="${isDecided}">&#10003;</c:when><c:otherwise>2</c:otherwise></c:choose></span>
        <span class="step-label">Submitted</span>
    </div>
    <div class="step-line ${isApproved ? 'is-approved' : (isRejected ? 'is-rejected' : '')}"></div>
    <div class="step ${isApproved ? 'is-approved' : (isRejected ? 'is-rejected' : '')}">
        <span class="step-dot">3</span>
        <span class="step-label"><c:choose><c:when test="${isApproved}">Approved</c:when><c:when test="${isRejected}">Rejected</c:when><c:otherwise>Decision</c:otherwise></c:choose></span>
    </div>
</div>

<table class="fields">
    <tr><th>Customer</th><td>${order.customerName}</td></tr>
    <tr><th>Status</th><td><span class="status status-${order.status}">${order.status}</span></td></tr>
    <tr><th>Created</th><td><fmt:formatDate value="${order.createdDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td></tr>
</table>

<h2>Line Items</h2>
<table class="grid">
    <thead>
    <tr>
        <th>Product</th>
        <th>Qty</th>
        <th>Unit Price</th>
        <th>Subtotal</th>
        <th>Discount</th>
        <th>Line Total</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="item" items="${order.lineItems}">
        <tr>
            <td>${item.productName}</td>
            <td>${item.quantity}</td>
            <td>$<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/></td>
            <td>$<fmt:formatNumber value="${item.lineSubtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
            <td>$<fmt:formatNumber value="${item.lineDiscount}" minFractionDigits="2" maxFractionDigits="2"/></td>
            <td>$<fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
        </tr>
    </c:forEach>
    <c:if test="${empty order.lineItems}">
        <tr><td colspan="6">No line items yet.</td></tr>
    </c:if>
    </tbody>
</table>

<table class="fields totals">
    <tr><th>Subtotal</th><td>$<fmt:formatNumber value="${order.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
    <tr><th>Discount</th><td>-$<fmt:formatNumber value="${order.discountAmount}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
    <tr><th>Tax (8%)</th><td>$<fmt:formatNumber value="${order.taxAmount}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
    <tr><th>Total</th><td><strong>$<fmt:formatNumber value="${order.total}" minFractionDigits="2" maxFractionDigits="2"/></strong></td></tr>
</table>

<c:if test="${order.status == 'DRAFT'}">
    <h2>Add Line Item</h2>
    <form action="addLineItem.do" method="post">
        <input type="hidden" name="orderId" value="${order.id}"/>

        <label for="productId">Product</label>
        <select name="productId" id="productId">
            <c:forEach var="product" items="${products}">
                <option value="${product.id}">${product.name} ($<fmt:formatNumber value="${product.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/>/unit)</option>
            </c:forEach>
        </select>

        <label for="quantity">Quantity</label>
        <input type="number" name="quantity" id="quantity" min="1" value="1"/>
        <span class="hint">10+ units gets a 10% bulk discount on that line</span>

        <button type="submit">Add Item</button>
    </form>

    <c:if test="${not empty order.lineItems}">
        <form action="submitOrder.do" method="post">
            <input type="hidden" name="id" value="${order.id}"/>
            <button type="submit">Submit Order for Approval</button>
        </form>
    </c:if>
</c:if>

<c:if test="${order.status == 'SUBMITTED'}">
    <h2>Approval</h2>
    <div class="button-row">
        <form action="approveOrder.do" method="post">
            <input type="hidden" name="id" value="${order.id}"/>
            <button type="submit">Approve</button>
        </form>
        <form action="rejectOrder.do" method="post">
            <input type="hidden" name="id" value="${order.id}"/>
            <button type="submit">Reject</button>
        </form>
    </div>
</c:if>

<p><a href="orders.do">&laquo; Back to order list</a></p>

</div>
</body>
</html>
