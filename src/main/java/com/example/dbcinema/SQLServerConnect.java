package com.example.dbcinema;

import java.sql.*;

public class SQLServerConnect {
    public static void main(String[] args) {
        String connectionUrl = "jdbc:sqlserver://Jarvis:1433;databaseName=Cinemadb;user=User1;password=root;encrypt=true;trustServerCertificate=true";

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {
            System.out.println("Connected to SQL Server successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
