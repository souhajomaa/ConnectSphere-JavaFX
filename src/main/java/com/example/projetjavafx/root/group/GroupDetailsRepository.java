package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GroupDetailsRepository {
    public ResultSet getGroupDetails(int groupId) {
        String query = "SELECT * FROM UserGroups WHERE group_id = ?";
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
