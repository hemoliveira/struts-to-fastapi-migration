package com.orderco.legacy.service;

import com.orderco.legacy.dao.ProductDao;
import com.orderco.legacy.dao.PurchaseOrderDao;
import com.orderco.legacy.model.Product;
import com.orderco.legacy.model.PurchaseOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * Order business logic, isolated from both the web tier (Struts Actions)
 * and the persistence tier (DAOs) -- the same separation of concerns an EJB
 * session bean would enforce, just as a plain Java class instead of a
 * container-managed bean. See the top-level README for why this demo
 * doesn't stand up a full Java EE application server to host a literal
 * @Stateless session bean.
 *
 * Business rules implemented here (and re-implemented identically on the
 * modernized side -- see MIGRATION.md):
 *   - A line item with quantity >= BULK_DISCOUNT_THRESHOLD gets a
 *     BULK_DISCOUNT_RATE discount on that line's subtotal.
 *   - Tax is a flat TAX_RATE applied to (subtotal - discount).
 *   - Orders can only be submitted from DRAFT, and only approved/rejected
 *     from SUBMITTED.
 */
public class OrderService {

    public static final int BULK_DISCOUNT_THRESHOLD = 10;
    public static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.10");
    public static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private final PurchaseOrderDao orderDao = new PurchaseOrderDao();
    private final ProductDao productDao = new ProductDao();

    public long createOrder(long customerId) throws SQLException {
        return orderDao.insertOrder(customerId);
    }

    public void addLineItem(long orderId, long productId, int quantity) throws SQLException, BusinessRuleException {
        PurchaseOrder order = orderDao.findById(orderId);
        if (order == null) {
            throw new BusinessRuleException("Order " + orderId + " does not exist.");
        }
        if (!PurchaseOrder.STATUS_DRAFT.equals(order.getStatus())) {
            throw new BusinessRuleException("Line items can only be added while an order is in DRAFT status.");
        }
        if (quantity <= 0) {
            throw new BusinessRuleException("Quantity must be a positive number.");
        }

        Product product = productDao.findById(productId);
        if (product == null) {
            throw new BusinessRuleException("Product " + productId + " does not exist.");
        }

        BigDecimal lineSubtotal = product.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal lineDiscount = calculateLineDiscount(quantity, lineSubtotal);
        BigDecimal lineTotal = lineSubtotal.subtract(lineDiscount).setScale(2, RoundingMode.HALF_UP);

        orderDao.insertLineItem(orderId, productId, quantity, product.getUnitPrice(), lineSubtotal, lineDiscount, lineTotal);
        recalculateOrderTotals(orderId);
    }

    /**
     * Bulk-discount rule: quantity >= BULK_DISCOUNT_THRESHOLD earns
     * BULK_DISCOUNT_RATE off that line's subtotal.
     */
    BigDecimal calculateLineDiscount(int quantity, BigDecimal lineSubtotal) {
        if (quantity >= BULK_DISCOUNT_THRESHOLD) {
            return lineSubtotal.multiply(BULK_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private void recalculateOrderTotals(long orderId) throws SQLException {
        PurchaseOrder order = orderDao.findById(orderId);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        for (int i = 0; i < order.getLineItems().size(); i++) {
            subtotal = subtotal.add(order.getLineItems().get(i).getLineSubtotal());
            discount = discount.add(order.getLineItems().get(i).getLineDiscount());
        }

        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = taxable.add(tax).setScale(2, RoundingMode.HALF_UP);

        orderDao.updateOrderTotals(orderId, subtotal.setScale(2, RoundingMode.HALF_UP),
                discount.setScale(2, RoundingMode.HALF_UP), tax, total);
    }

    public void submitOrder(long orderId) throws SQLException, BusinessRuleException {
        PurchaseOrder order = orderDao.findById(orderId);
        if (order == null) {
            throw new BusinessRuleException("Order " + orderId + " does not exist.");
        }
        if (!PurchaseOrder.STATUS_DRAFT.equals(order.getStatus())) {
            throw new BusinessRuleException("Only a DRAFT order can be submitted.");
        }
        if (order.getLineItems().isEmpty()) {
            throw new BusinessRuleException("Cannot submit an order with no line items.");
        }
        orderDao.updateStatus(orderId, PurchaseOrder.STATUS_SUBMITTED);
    }

    public void approveOrder(long orderId) throws SQLException, BusinessRuleException {
        transitionFromSubmitted(orderId, PurchaseOrder.STATUS_APPROVED);
    }

    public void rejectOrder(long orderId) throws SQLException, BusinessRuleException {
        transitionFromSubmitted(orderId, PurchaseOrder.STATUS_REJECTED);
    }

    private void transitionFromSubmitted(long orderId, String targetStatus) throws SQLException, BusinessRuleException {
        PurchaseOrder order = orderDao.findById(orderId);
        if (order == null) {
            throw new BusinessRuleException("Order " + orderId + " does not exist.");
        }
        if (!PurchaseOrder.STATUS_SUBMITTED.equals(order.getStatus())) {
            throw new BusinessRuleException("Only a SUBMITTED order can be approved or rejected.");
        }
        orderDao.updateStatus(orderId, targetStatus);
    }

    public PurchaseOrder getOrder(long orderId) throws SQLException {
        return orderDao.findById(orderId);
    }

    public List<PurchaseOrder> listOrders() throws SQLException {
        return orderDao.findAll();
    }
}
