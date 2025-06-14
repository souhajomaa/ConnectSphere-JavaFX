package com.example.projetjavafx.root.events;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CreateEventsRepository {
    public static ObservableList<String> getOrganizers() {
        ObservableList<String> organizers = FXCollections.observableArrayList();
        String sql = "SELECT id, username FROM users"; // Adapte selon ta table Users

        try (Connection connection = AivenMySQLManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {
            while (resultSet.next()) {
                String organizer = resultSet.getInt("user_id") + " - " + resultSet.getString("username");
                organizers.add(organizer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return organizers;
    }



    public static void createEvent(String name, String description, String startTime, String endTime,
                                   String location, int categoryId, String imagePath) throws SQLException {
        String sql = "INSERT INTO events (name, description, start_time, end_time, location, organizer_id_id, category_id_id, image) " +
                "VALUES (?, ?, ?, ?, ?, 1, ?, ?)";

        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, startTime);
            statement.setString(4, endTime);
            statement.setString(5, location);
            statement.setInt(6, categoryId); // Devient le 6ème paramètre
            statement.setString(7, imagePath); // 7ème paramètre


            statement.executeUpdate();
        }
    }
}
