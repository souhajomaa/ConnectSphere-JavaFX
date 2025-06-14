package com.example.projetjavafx.root.social;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostDAO {
    private Connection connection;

    public PostDAO(Connection connection) {
        this.connection = connection;
    }

    public void addPost(Post post) throws SQLException {
        String sql = "INSERT INTO FeedPosts (user_id, content, created_at, image_path, event_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, post.getUserId());
            statement.setString(2, post.getContent());
            statement.setTimestamp(3, new Timestamp(post.getCreatedAt().getTime()));
            statement.setString(4, post.getImagePath());
            
            // Set event_id if it exists, otherwise set to null
            if (post.getEventId() > 0) {
                statement.setInt(5, post.getEventId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        post.setPostId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    // Modify the getAllPosts method to handle the timestamp format issue
    public List<Post> getAllPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "(SELECT COUNT(*) FROM Likes WHERE post_id = p.post_id) as like_count, " +
                "(SELECT COUNT(*) FROM Comments WHERE post_id = p.post_id) as comment_count " +
                "FROM FeedPosts p ORDER BY p.created_at DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                // Handle timestamp conversion safely
                Date createdAt = null;
                try {
                    String timestampStr = resultSet.getString("created_at");
                    if (timestampStr != null && !timestampStr.isEmpty()) {
                        // Try to parse the timestamp string
                        if (timestampStr.contains("T")) {
                            // Handle ISO format like "2025-02-26T01:53:07.617660"
                            timestampStr = timestampStr.replace('T', ' ').split("\\.")[0];
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            createdAt = new Date(sdf.parse(timestampStr).getTime());
                        } else {
                            // Try standard timestamp format
                            createdAt = resultSet.getTimestamp("created_at");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing timestamp: " + e.getMessage());
                    // Use current time as fallback
                    createdAt = new Date();
                }

                Post post = new Post(
                        resultSet.getInt("post_id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("content"),
                        createdAt,
                        resultSet.getString("image_path"),
                        resultSet.getInt("like_count"),
                        resultSet.getInt("comment_count")
                );
                
                // Récupérer l'ID de l'événement s'il existe
                if (resultSet.getObject("event_id") != null) {
                    post.setEventId(resultSet.getInt("event_id"));
                }
                
                // Récupérer l'ID du groupe s'il existe
                if (resultSet.getObject("group_id") != null) {
                    post.setGroupId(resultSet.getInt("group_id"));
                }
                
                posts.add(post);
            }
        }

        return posts;
    }

    public List<Post> searchPosts(String query) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "(SELECT COUNT(*) FROM Likes WHERE post_id = p.post_id) as like_count, " +
                "(SELECT COUNT(*) FROM Comments WHERE post_id = p.post_id) as comment_count " +
                "FROM FeedPosts p WHERE p.content LIKE ? ORDER BY p.created_at DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + query + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Post post = new Post(
                            resultSet.getInt("post_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("content"),
                            resultSet.getTimestamp("created_at"),
                            resultSet.getString("image_path"),
                            resultSet.getInt("like_count"),
                            resultSet.getInt("comment_count")
                    );
                    posts.add(post);
                }
            }
        }

        return posts;
    }

    public Post getPostById(int postId) throws SQLException {
        String sql = "SELECT p.*, " +
                "(SELECT COUNT(*) FROM Likes WHERE post_id = p.post_id) as like_count, " +
                "(SELECT COUNT(*) FROM Comments WHERE post_id = p.post_id) as comment_count " +
                "FROM FeedPosts p WHERE p.post_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, postId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Post(
                            resultSet.getInt("post_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("content"),
                            resultSet.getTimestamp("created_at"),
                            resultSet.getString("image_path"),
                            resultSet.getInt("like_count"),
                            resultSet.getInt("comment_count")
                    );
                }
            }
        }

        return null;
    }

    // Add this method to your PostDAO class
    public List<Post> getPostsByGroupId(int groupId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "(SELECT COUNT(*) FROM Likes WHERE post_id = p.post_id) as like_count, " +
                "(SELECT COUNT(*) FROM Comments WHERE post_id = p.post_id) as comment_count " +
                "FROM FeedPosts p WHERE p.group_id = ? ORDER BY p.created_at DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Post post = new Post(
                            resultSet.getInt("post_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("content"),
                            resultSet.getTimestamp("created_at"),
                            resultSet.getString("image_path"),
                            resultSet.getInt("like_count"),
                            resultSet.getInt("comment_count")
                    );
                    post.setGroupId(resultSet.getInt("group_id"));
                    posts.add(post);
                }
            }
        }

        return posts;
    }

    /**
     * Gets the like count for a specific post
     *
     * @param postId The ID of the post
     * @return The number of likes for the post
     */
    public int getLikeCount(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Likes WHERE post_id = ?";

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
     * Checks if a user has liked a specific post
     *
     * @param postId The ID of the post
     * @param userId The ID of the user
     * @return true if the user has liked the post, false otherwise
     */
    public boolean hasUserLikedPost(int postId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Likes WHERE post_id = ? AND user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, postId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Toggles the like status for a post by a user
     *
     * @param postId The ID of the post
     * @param userId The ID of the user
     * @return true if the post is now liked, false if it was unliked
     */
    public boolean toggleLike(int postId, int userId) throws SQLException {
        boolean currentlyLiked = hasUserLikedPost(postId, userId);

        if (currentlyLiked) {
            // Unlike the post
            String sql = "DELETE FROM Likes WHERE post_id = ? AND user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, postId);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }
            return false;
        } else {
            // Like the post
            String sql = "INSERT INTO Likes (post_id, user_id, created_at) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, postId);
                statement.setInt(2, userId);
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
            return true;
        }
    }

    /**
     * Saves a post to the database
     *
     * @param post The post to save
     * @return The saved post with its ID set
     */
    public Post savePost(Post post) throws SQLException {
        if (post.getPostId() > 0) {
            // Update existing post
            String sql = "UPDATE FeedPosts SET user_id = ?, content = ?, image_path = ?, group_id = ? WHERE post_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, post.getUserId());
                statement.setString(2, post.getContent());
                statement.setString(3, post.getImagePath());
                statement.setInt(4, post.getGroupId());
                statement.setInt(5, post.getPostId());

                statement.executeUpdate();
            }
        } else {
            // Insert new post
            String sql = "INSERT INTO FeedPosts (user_id, content, created_at, image_path, group_id) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, post.getUserId());
                statement.setString(2, post.getContent());
                statement.setTimestamp(3, new Timestamp(post.getCreatedAt().getTime()));
                statement.setString(4, post.getImagePath());
                statement.setInt(5, post.getGroupId());

                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            post.setPostId(generatedKeys.getInt(1));
                        }
                    }
                }
            }
        }

        return post;
    }

    public void deletePost(int postId) throws SQLException {
        String sql = "DELETE FROM FeedPosts WHERE post_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, postId);
            statement.executeUpdate();
        }
    }

    public void updatePost(int postId, String newContent, String newImagePath) throws SQLException {
        String sql = "UPDATE FeedPosts SET content = ?, image_path = ? WHERE post_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newContent);
            statement.setString(2, newImagePath);
            statement.setInt(3, postId);
            statement.executeUpdate();
        }
    }
    
    /**
     * Gets all posts by a specific user
     *
     * @param userId The ID of the user
     * @return A list of posts by the user
     */
    public List<Post> getPostsByUserId(int userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "(SELECT COUNT(*) FROM Likes WHERE post_id = p.post_id) as like_count, " +
                "(SELECT COUNT(*) FROM Comments WHERE post_id = p.post_id) as comment_count " +
                "FROM FeedPosts p WHERE p.user_id = ? ORDER BY p.created_at DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Handle timestamp conversion safely
                    Date createdAt = null;
                    try {
                        String timestampStr = resultSet.getString("created_at");
                        if (timestampStr != null && !timestampStr.isEmpty()) {
                            // Try to parse the timestamp string
                            if (timestampStr.contains("T")) {
                                // Handle ISO format like "2025-02-26T01:53:07.617660"
                                timestampStr = timestampStr.replace('T', ' ').split("\\.")[0];
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                createdAt = new Date(sdf.parse(timestampStr).getTime());
                            } else {
                                // Try standard timestamp format
                                createdAt = resultSet.getTimestamp("created_at");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing timestamp: " + e.getMessage());
                        // Use current time as fallback
                        createdAt = new Date();
                    }

                    Post post = new Post(
                            resultSet.getInt("post_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("content"),
                            createdAt,
                            resultSet.getString("image_path"),
                            resultSet.getInt("like_count"),
                            resultSet.getInt("comment_count")
                    );
                    posts.add(post);
                }
            }
        }

        return posts;
    }

}
