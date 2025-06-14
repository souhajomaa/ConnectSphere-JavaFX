package com.example.projetjavafx.root.social;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private Connection connection;

    public CommentDAO(Connection connection) {
        this.connection = connection;
    }

    public void addComment(int postId, int userId, String content) throws SQLException {
        String sql = "INSERT INTO Comments (post_id, user_id, content, time_stamp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, postId);
            statement.setInt(2, userId);
            statement.setString(3, content);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            statement.executeUpdate();
        }
    }

    public List<Comment> getCommentsForPost(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM Comments WHERE post_id = ? ORDER BY time_stamp ASC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment(rs.getInt("id"), rs.getInt("post_id"),
                        rs.getInt("user_id"), rs.getString("content"),
                        rs.getTimestamp("time_stamp"));
                comments.add(comment);
            }
        }
        return comments;
    }
    public int getCommentCount(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Comments WHERE post_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, postId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * Deletes a comment from the database
     * @param commentId The ID of the comment to delete
     * @return true if the comment was successfully deleted, false otherwise
     */
    public boolean deleteComment(int commentId) throws SQLException {
        String sql = "DELETE FROM Comments WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, commentId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Updates a comment in the database
     * @param commentId The ID of the comment to update
     * @param newContent The new content of the comment
     * @throws SQLException if a database access error occurs
     */
    public void updateComment(int commentId, String newContent) throws SQLException {
        String sql = "UPDATE Comments SET content = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newContent);
            statement.setInt(2, commentId);
            statement.executeUpdate();
        }
    }

}