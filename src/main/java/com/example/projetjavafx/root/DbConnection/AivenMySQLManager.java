package com.example.projetjavafx.root.DbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AivenMySQLManager {
    private static final String URL = "jdbc:mysql://mysql-1dcac8df-moamedsalahsaoudi123-c05d.e.aivencloud.com:22451/defaultdb?ssl-mode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_5qB58jyOaJs3WW0eYS9";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}