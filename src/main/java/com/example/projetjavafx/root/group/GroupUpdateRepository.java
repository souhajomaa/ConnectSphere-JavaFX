package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupUpdateRepository {
    // Modèle (Model)
    private String name;
    private String description;

    // Constructeur
    public GroupUpdateRepository(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Méthode pour charger les données d'un groupe
    public GroupUpdateRepository loadGroupData(int groupId) {
        String query = "SELECT name, description FROM user_groups WHERE id = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, groupId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new GroupUpdateRepository(
                        resultSet.getString("name"),
                        resultSet.getString("description")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode pour mettre à jour un groupe
    public boolean updateGroup(int groupId, String name, String description) {
        String query = "UPDATE user_groups SET name = ?, description = ? WHERE id = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setInt(3, groupId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
