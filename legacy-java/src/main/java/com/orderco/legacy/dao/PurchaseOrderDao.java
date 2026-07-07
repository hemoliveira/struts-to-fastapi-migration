package com.orderco.legacy.dao;

import com.orderco.legacy.model.OrderLineItem;
import com.orderco.legacy.model.PurchaseOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDao {

    public long insertOrder(long customerId) throws SQLException {
        String sql = "INSERT INTO purchase_order (customer_id, status, created_date, subtotal, discount_amount, tax_amount, total) "
                + "VALUES (?, ?, CURRENT_TIMESTAMP, 0, 0, 0, 0)";

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            try {
                stmt.setLong(1, customerId);
                stmt.setString(2, PurchaseOrder.STATUS_DRAFT);
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                try {
                    keys.next();
                    return keys.getLong(1);
                } finally {
                    keys.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }

    public void insertLineItem(long orderId, long productId, int quantity, java.math.BigDecimal unitPrice,
                                java.math.BigDecimal lineSubtotal, java.math.BigDecimal lineDiscount,
                                java.math.BigDecimal lineTotal) throws SQLException {
        String sql = "INSERT INTO order_line_item (order_id, product_id, quantity, unit_price, line_subtotal, line_discount, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setLong(1, orderId);
                stmt.setLong(2, productId);
                stmt.setInt(3, quantity);
                stmt.setBigDecimal(4, unitPrice);
                stmt.setBigDecimal(5, lineSubtotal);
                stmt.setBigDecimal(6, lineDiscount);
                stmt.setBigDecimal(7, lineTotal);
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }

    public void updateOrderTotals(long orderId, java.math.BigDecimal subtotal, java.math.BigDecimal discountAmount,
                                   java.math.BigDecimal taxAmount, java.math.BigDecimal total) throws SQLException {
        String sql = "UPDATE purchase_order SET subtotal = ?, discount_amount = ?, tax_amount = ?, total = ? WHERE id = ?";

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setBigDecimal(1, subtotal);
                stmt.setBigDecimal(2, discountAmount);
                stmt.setBigDecimal(3, taxAmount);
                stmt.setBigDecimal(4, total);
                stmt.setLong(5, orderId);
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }

    public void updateStatus(long orderId, String status) throws SQLException {
        String sql = "UPDATE purchase_order SET status = ? WHERE id = ?";

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setString(1, status);
                stmt.setLong(2, orderId);
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }

    public List<PurchaseOrder> findAll() throws SQLException {
        String sql = "SELECT po.id, po.customer_id, c.name AS customer_name, po.status, po.created_date, "
                + "po.subtotal, po.discount_amount, po.tax_amount, po.total "
                + "FROM purchase_order po JOIN customer c ON c.id = po.customer_id "
                + "ORDER BY po.id DESC";

        List<PurchaseOrder> results = new ArrayList<PurchaseOrder>();

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                ResultSet rs = stmt.executeQuery();
                try {
                    while (rs.next()) {
                        results.add(mapOrderRow(rs));
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
        return results;
    }

    public PurchaseOrder findById(long id) throws SQLException {
        String sql = "SELECT po.id, po.customer_id, c.name AS customer_name, po.status, po.created_date, "
                + "po.subtotal, po.discount_amount, po.tax_amount, po.total "
                + "FROM purchase_order po JOIN customer c ON c.id = po.customer_id "
                + "WHERE po.id = ?";

        PurchaseOrder order;
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                try {
                    if (!rs.next()) {
                        return null;
                    }
                    order = mapOrderRow(rs);
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }

        order.setLineItems(findLineItems(id));
        return order;
    }

    private List<OrderLineItem> findLineItems(long orderId) throws SQLException {
        String sql = "SELECT oli.id, oli.order_id, oli.product_id, p.name AS product_name, oli.quantity, "
                + "oli.unit_price, oli.line_subtotal, oli.line_discount, oli.line_total "
                + "FROM order_line_item oli JOIN product p ON p.id = oli.product_id "
                + "WHERE oli.order_id = ? ORDER BY oli.id";

        List<OrderLineItem> results = new ArrayList<OrderLineItem>();

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setLong(1, orderId);
                ResultSet rs = stmt.executeQuery();
                try {
                    while (rs.next()) {
                        OrderLineItem item = new OrderLineItem();
                        item.setId(rs.getLong("id"));
                        item.setOrderId(rs.getLong("order_id"));
                        item.setProductId(rs.getLong("product_id"));
                        item.setProductName(rs.getString("product_name"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getBigDecimal("unit_price"));
                        item.setLineSubtotal(rs.getBigDecimal("line_subtotal"));
                        item.setLineDiscount(rs.getBigDecimal("line_discount"));
                        item.setLineTotal(rs.getBigDecimal("line_total"));
                        results.add(item);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
        return results;
    }

    private PurchaseOrder mapOrderRow(ResultSet rs) throws SQLException {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(rs.getLong("id"));
        order.setCustomerId(rs.getLong("customer_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setStatus(rs.getString("status"));
        order.setCreatedDate(rs.getTimestamp("created_date"));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setTaxAmount(rs.getBigDecimal("tax_amount"));
        order.setTotal(rs.getBigDecimal("total"));
        return order;
    }
}
