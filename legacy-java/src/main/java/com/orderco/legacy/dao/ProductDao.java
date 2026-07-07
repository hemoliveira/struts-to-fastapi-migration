package com.orderco.legacy.dao;

import com.orderco.legacy.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public List<Product> findAll() throws SQLException {
        String sql = "SELECT id, sku, name, unit_price FROM product ORDER BY name";
        List<Product> results = new ArrayList<Product>();

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                ResultSet rs = stmt.executeQuery();
                try {
                    while (rs.next()) {
                        results.add(mapRow(rs));
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

    public Product findById(long id) throws SQLException {
        String sql = "SELECT id, sku, name, unit_price FROM product WHERE id = ?";

        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                try {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                    return null;
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setSku(rs.getString("sku"));
        product.setName(rs.getString("name"));
        product.setUnitPrice(rs.getBigDecimal("unit_price"));
        return product;
    }
}
