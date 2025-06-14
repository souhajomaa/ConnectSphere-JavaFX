package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupAddMemberRepository {

    public ResultSet searchUsers(String searchText) {
        String query = "SELECT id, username FROM users WHERE username LIKE ?";
        try {
            Connection connection = AivenMySQLManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%" + searchText + "%");
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addMemberToGroup(int groupId, int userId, String role) {
        if (isUserMemberOfGroup(groupId, userId)) {
            System.out.println("User is already a member of the group.");
            return false;
        }

        String query = "INSERT INTO group_members (group_it_id, user_id_id, role, status) VALUES (?, ?, ?, 'pending')";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            statement.setString(3, role);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUserMemberOfGroup(int groupId, int userId) {
        String query = "SELECT COUNT(*) FROM group_members WHERE group_it_id = ? AND user_id_id = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isUserGroupCreator(int groupId, int userId) {
        String query = "SELECT COUNT(*) FROM group_members WHERE group_it_id = ? AND user_id_id = ? AND role = 'admin'";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeUserFromGroup(int groupId, int userId) {
        String query = "DELETE FROM group_members WHERE group_it_id = ? AND user_id_id = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getGroupMemberCount(int groupId) {
        String query = "SELECT COUNT(*) FROM group_members WHERE group_it_id = ?";
        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
