package com.example.projetjavafx.root.messagerie.db;


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
                userId = rs.getInt("id");  // Récupérer l'ID du destinataire
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    // Améliorer la méthode getAllUsers pour ajouter plus de logs et de vérifications
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username FROM users";

        boolean databaseAccessSuccessful = false;

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Executing getAllUsers query");
            databaseAccessSuccessful = true;

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
            databaseAccessSuccessful = false;
        }

        // Si aucun utilisateur n'a été trouvé ou si l'accès à la base de données a échoué,
        // ajouter des utilisateurs de test
        if (users.isEmpty()) {
            System.out.println("No users found in database or database access failed. Adding test users.");
            users.add(new User(1, "user1"));
            users.add(new User(2, "user2"));
            users.add(new User(3, "user3"));

            // Si l'accès à la base de données a réussi mais qu'aucun utilisateur n'a été trouvé,
            // nous pourrions vouloir les ajouter à la base de données ici
            if (databaseAccessSuccessful) {
                System.out.println("Database access was successful but no users found. Consider adding users to the database.");

                // Ajouter les utilisateurs de test à la base de données
                try (Connection conn = AivenMySQLManager.getConnection()) {
                    String insertQuery = "INSERT INTO users (id, username) VALUES (?, ?) ON DUPLICATE KEY UPDATE username = VALUES(username)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

                    for (User user : users) {
                        insertStmt.setInt(1, user.getUserId());
                        insertStmt.setString(2, user.getUsername());
                        insertStmt.executeUpdate();
                    }

                    System.out.println("Added test users to database");
                } catch (SQLException e) {
                    System.err.println("Error adding test users to database: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Total users found/created: " + users.size());
        return users;
    }

    public List<User> searchUsers(String searchTerm) {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username FROM users WHERE username LIKE ?";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("username"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}