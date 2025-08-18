package com.pahanaedu.service.db;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Tries JNDI first (jdbc/pahanaedu, then java:comp/env/jdbc/pahanaedu).
 * If not found, falls back to DriverManager using system properties:
 *  -DPAHANA_JDBC_URL=jdbc:mysql://localhost:3306/pahanaedu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
 *  -DPAHANA_JDBC_USER=root
 *  -DPAHANA_JDBC_PASS=
 */
public class Database {

    private static DataSource tryLookup() throws SQLException {
        try {
            InitialContext ctx = new InitialContext();
            try {
                return (DataSource) ctx.lookup("jdbc/pahanaedu"); // global JNDI
            } catch (NamingException ignore) {
                return (DataSource) ctx.lookup("java:comp/env/jdbc/pahanaedu"); // component env
            }
        } catch (NamingException e) {
            return null; // fall back to DriverManager
        }
    }

    public static Connection getConnection() throws SQLException {
        DataSource ds = tryLookup();
        if (ds != null) {
            return ds.getConnection();
        }
        // --- Fallback: direct JDBC for local dev ---
        String url  = System.getProperty(
            "PAHANA_JDBC_URL",
            "jdbc:mysql://localhost:3306/pahanaedu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        String user = System.getProperty("PAHANA_JDBC_USER", "root");
        String pass = System.getProperty("PAHANA_JDBC_PASS", "");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL driver not found. Put mysql-connector-j in domain/lib.", ex);
        }
        return java.sql.DriverManager.getConnection(url, user, pass);
    }
}