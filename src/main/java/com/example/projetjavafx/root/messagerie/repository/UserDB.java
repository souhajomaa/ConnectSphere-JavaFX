package com.example.projetjavafx.root.messagerie.repository;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.messagerie.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UserDB {

    public int getUserIdByUsername(String username) {
        int userId = -1;
        String query = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("user_id");  // Récupérer l'ID du destinataire
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username FROM users";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Executing getAllUsers query");

            while (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");

                System.out.println("Found user: " + userId + " - " + username);

                User user = new User(userId, username);
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Database error in getAllUsers: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Total users found: " + users.size());
        return users;
    }

    public List<User> searchUsers(String searchTerm) {
        List<User> users = new ArrayList<>();
        String query = "SELECT user_id, username FROM Users WHERE username LIKE ?";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User(rs.getInt("user_id"), rs.getString("username"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}

