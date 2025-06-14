package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GroupMembersRepository {

    public ResultSet getGroupMembers(int groupId) {
        String query = "SELECT u.username, gm.role " +
                "FROM group_members gm " +
                "JOIN users u ON gm.user_id_id = u.id " +
                "WHERE gm.group_it_id = ?";
        try {
            Connection connection = AivenMySQLManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, groupId);
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}