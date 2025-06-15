package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginRepository {

    private Connection connect() throws SQLException {
        return AivenMySQLManager.getConnection();
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
