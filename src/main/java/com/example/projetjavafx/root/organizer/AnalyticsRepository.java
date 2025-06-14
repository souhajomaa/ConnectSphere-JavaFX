package com.example.projetjavafx.root.organizer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

public class AnalyticsRepository {

    public static List<EventStats> getEventStats(int organizerId) throws SQLException {
        List<EventStats> stats = new ArrayList<>();
        String sql = "SELECT e.event_id, e.name, "
                + "COUNT(p.id) AS total, "
                + "SUM(CASE WHEN u.gender = 'Male' THEN 1 ELSE 0 END) AS male, "
                + "SUM(CASE WHEN u.gender = 'Female' THEN 1 ELSE 0 END) AS female "
                + "FROM Events e "
                + "LEFT JOIN participation p ON e.event_id = p.event_id "
                + "LEFT JOIN Users u ON p.participant_id = u.user_id "
                + "WHERE e.organizer_id = ? "
                + "GROUP BY e.event_id, e.name";  // Modified GROUP BY clause

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, organizerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                stats.add(new EventStats(
                        rs.getInt("event_id"),
                        rs.getString("name"),
                        rs.getInt("total"),
                        rs.getInt("male"),
                        rs.getInt("female")
                ));
            }
        }
        return stats;
    }

    public static List<EventAgeStats> getEventAvgAgeStats(int organizerId) throws SQLException {
        List<EventAgeStats> stats = new ArrayList<>();
        String sql = "SELECT e.event_id, e.name, AVG(u.age) AS avg_age "
                + "FROM Events e "
                + "LEFT JOIN participation p ON e.event_id = p.event_id "
                + "LEFT JOIN Users u ON p.participant_id = u.user_id "
                + "WHERE e.organizer_id = ? "
                + "GROUP BY e.event_id, e.name";  // Modified GROUP BY clause

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, organizerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                stats.add(new EventAgeStats(
                        rs.getInt("event_id"),
                        rs.getString("name"),
                        rs.getDouble("avg_age")
                ));
            }
        }
        return stats;
    }
}