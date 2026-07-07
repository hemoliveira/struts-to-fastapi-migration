package com.orderco.legacy.util;

import com.orderco.legacy.dao.ConnectionManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Runs schema.sql and seeds reference data on startup. Stands in for the
 * DBA-managed Oracle schema + seed scripts this would have in production.
 */
public class DatabaseInitListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        try {
            runSchema();
            seedIfEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize the database", e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        // no-op
    }

    private void runSchema() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql");
        if (in == null) {
            throw new IllegalStateException("schema.sql not found on classpath");
        }

        StringBuilder sql = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Strip full-line "--" comments before splitting on ";" below,
                // so punctuation inside a comment can never be mistaken for a
                // statement terminator.
                if (line.trim().startsWith("--")) {
                    continue;
                }
                sql.append(line).append("\n");
            }
        } finally {
            reader.close();
        }

        Connection conn = ConnectionManager.getConnection();
        try {
            Statement stmt = conn.createStatement();
            try {
                String[] statements = sql.toString().split(";");
                for (int i = 0; i < statements.length; i++) {
                    String s = statements[i].trim();
                    if (s.length() > 0) {
                        stmt.execute(s);
                    }
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }

    private void seedIfEmpty() throws Exception {
        Connection conn = ConnectionManager.getConnection();
        try {
            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customer");
                rs.next();
                int count = rs.getInt(1);
                rs.close();

                if (count > 0) {
                    return;
                }

                stmt.execute("INSERT INTO customer (name, email) VALUES ('Acme Retail Corp', 'purchasing@acmeretail.example')");
                stmt.execute("INSERT INTO customer (name, email) VALUES ('Blue Ridge Distributors', 'orders@blueridge.example')");
                stmt.execute("INSERT INTO customer (name, email) VALUES ('Coastal Supply Co', 'ap@coastalsupply.example')");

                stmt.execute("INSERT INTO product (sku, name, unit_price) VALUES ('WID-100', 'Standard Widget', 12.50)");
                stmt.execute("INSERT INTO product (sku, name, unit_price) VALUES ('WID-200', 'Heavy-Duty Widget', 24.00)");
                stmt.execute("INSERT INTO product (sku, name, unit_price) VALUES ('GAD-300', 'Premium Gadget', 89.99)");
                stmt.execute("INSERT INTO product (sku, name, unit_price) VALUES ('GAD-400', 'Economy Gadget', 15.75)");
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }
}
