package org.example.cinemadb;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLServerConnect {
    private static final String URL = "jdbc:sqlserver://LAPTOP-859490JA:37233;databaseName=Project;encrypt=true;trustServerCertificate=true";
    private static final String USER = "User1";
    private static final String PASS = "root";

    public SQLServerConnect() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("connection established!");
            return DriverManager.getConnection("jdbc:sqlserver://LAPTOP-859490JA:37233;databaseName=Project;encrypt=true;trustServerCertificate=true", "User1", "root");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
}