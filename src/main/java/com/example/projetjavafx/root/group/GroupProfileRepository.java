package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupProfileRepository {
    // Modèle (Model)
    private String name;
    private String description;
    private String rules;
    private String profilePicture;

    // Constructeur
    public GroupProfileRepository(String name, String description, String rules, String profilePicture) {
        this.name = name;
        this.description = description;
        this.rules = rules;
        this.profilePicture = profilePicture;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRules() {
        return rules;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    // Méthode pour charger les groupes
    public ResultSet getGroups(String searchText) {
        String query = "SELECT name, description, rules, profile_picture FROM user_groups";
        if (searchText != null && !searchText.isEmpty()) {
            query += " WHERE name LIKE ? OR description LIKE ?";
        }

        try {
            Connection connection = AivenMySQLManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            if (searchText != null && !searchText.isEmpty()) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                statement.setString(1, searchPattern);
                statement.setString(2, searchPattern);
            }

            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode pour supprimer un groupe
    public boolean deleteGroup(int groupId) {
        String query = "DELETE FROM user_groups WHERE id = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, groupId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Méthode pour récupérer l'ID d'un groupe par son nom
    public int getGroupIdByName(String groupName) {
        String query = "SELECT id FROM user_groups WHERE name = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, groupName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Méthode pour récupérer l'ID d'un utilisateur par son nom d'utilisateur
    public int getUserIdByUsername(String username) {
        String query = "SELECT id FROM users WHERE username = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Méthode pour récupérer un groupe par son ID
    public GroupProfileRepository getGroupById(int groupId) {
        String query = "SELECT * FROM user_groups WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = AivenMySQLManager.getConnection();
            statement = connection.prepareStatement(query);
            statement.setInt(1, groupId);

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String rules = resultSet.getString("rules");
                String profilePicture = resultSet.getString("profile_picture");
                return new GroupProfileRepository(name, description, rules, profilePicture);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}