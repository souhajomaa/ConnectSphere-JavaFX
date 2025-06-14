package com.example.projetjavafx.root.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginRepository {
    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://mysql-1dcac8df-moamedsalahsaoudi123-c05d.e.aivencloud.com:22451/defaultdb?ssl-mode=REQUIRED";
        String user = "avnadmin";
        String pass = "AVNS_5qB58jyOaJs3WW0eYS9";
        return DriverManager.getConnection(url, user, pass);
    }

    // Méthode pour authentifier l'utilisateur et retourner un ResultSet
    public ResultSet authenticate(String email, String password) {
        String query = "SELECT id, email FROM users WHERE email = ? AND password = ?";

        try {
            Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
