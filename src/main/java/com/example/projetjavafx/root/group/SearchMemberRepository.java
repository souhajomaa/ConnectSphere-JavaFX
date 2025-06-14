package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SearchMemberRepository {
    public ResultSet searchUsers(String searchText) {
        String query = "SELECT id, username, email FROM users WHERE username LIKE ? OR email LIKE ?";
        try {
            Connection connection = AivenMySQLManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            String searchPattern = "%" + searchText + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);

            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
