package com.example.projetjavafx.root.social;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date; // Import correct

public class LikeDAO {
    private Connection connection;

    public LikeDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean isPostLikedByUser(int postId, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Likes WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }
    public void likePost(int postId, int userId) throws SQLException {
        String query = "INSERT IGNORE INTO Likes (post_id, user_id, time_stamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm"); // Format compatible avec VARCHAR
            String currentTime = sdf.format(new Date()); // Date actuelle sous forme de chaîne
            stmt.setString(3, currentTime);
            stmt.executeUpdate();
        }
    }    public void unlikePost(int postId, int userId) throws SQLException {
        String query = "DELETE FROM Likes WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
    public int getLikeCount(int postId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Likes WHERE post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }}