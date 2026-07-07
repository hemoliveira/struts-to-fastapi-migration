package com.orderco.legacy.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hands out JDBC connections against the embedded H2 database.
 *
 * In the production system this pattern is modeled on, this pointed at
 * Oracle via a connection pool managed by the app server. H2 is used here
 * (file-based, under WEB-INF/db) so the whole app runs standalone with no
 * external database to install -- see the top-level README for the reasoning.
 */
public final class ConnectionManager {

    private static final String URL = "jdbc:h2:./target/orderco-db/orderco;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found on classpath", e);
        }
    }

    private ConnectionManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
