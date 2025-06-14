package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {

    public boolean userExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public int insertUser(String username, String email, String password, int age, String sexe) throws SQLException {
        String query = "INSERT INTO users (username, email, password, age, gender) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setInt(4, age);
            stmt.setString(5, sexe);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);  // Retourne l'ID généré pour l'utilisateur
                }
            }
        }
        return -1;
    }
}