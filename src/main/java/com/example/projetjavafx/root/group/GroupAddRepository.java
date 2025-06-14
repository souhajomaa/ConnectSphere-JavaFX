package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.*;

public class GroupAddRepository {

    public boolean saveGroup(String name, String description, String imageBase64, int creatorId, String role) {
        // ✅ Correction des noms de tables et champs
        String insertGroupQuery = "INSERT INTO user_groups (name, description, profile_picture, creator_id_id, created_at) VALUES (?, ?, ?, ?, NOW())";
        String insertMemberQuery = "INSERT INTO group_members (group_it_id, user_id_id, role, status) VALUES (?, ?, ?, 'pending')";

        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement groupStatement = connection.prepareStatement(insertGroupQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement memberStatement = connection.prepareStatement(insertMemberQuery)) {

            // 1. Insert group
            groupStatement.setString(1, name);
            groupStatement.setString(2, description);
            groupStatement.setString(3, imageBase64);
            groupStatement.setInt(4, creatorId);
            groupStatement.executeUpdate();

            // 2. Get generated group ID
            ResultSet generatedKeys = groupStatement.getGeneratedKeys();
            int groupId = -1;
            if (generatedKeys.next()) {
                groupId = generatedKeys.getInt(1);
            }

            if (groupId == -1) {
                throw new SQLException("Failed to create the group, no ID obtained.");
            }

            // 3. Insert group member (creator)
            memberStatement.setInt(1, groupId);
            memberStatement.setInt(2, creatorId);
            memberStatement.setString(3, role);
            memberStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
