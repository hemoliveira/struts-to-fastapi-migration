package com.orderco.legacy.dao;

import com.orderco.legacy.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT id, name, email FROM customer ORDER BY name";
        List<Customer> results = new ArrayList<Customer>();

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

    public Customer findById(long id) throws SQLException {
        String sql = "SELECT id, name, email FROM customer WHERE id = ?";

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

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));
        return customer;
    }
}
