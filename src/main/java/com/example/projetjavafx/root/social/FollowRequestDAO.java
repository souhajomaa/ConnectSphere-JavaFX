package com.example.projetjavafx.root.social;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FollowRequestDAO {
    private Connection connection;
    
    public FollowRequestDAO(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }
    
    private void createTableIfNotExists() {
        // Table UserFollowers already exists in database
        // No need to create it
    }
    
    /**
     * Send a follow request
     */
    public boolean sendFollowRequest(int senderId, int receiverId) {
        // Check if request already exists
        if (requestExists(senderId, receiverId)) {
            return false;
        }
        
        String sql = "INSERT INTO UserFollowers (follower_id, followed_id, created_at, status) VALUES (?, ?, NOW(), 'pending')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error sending follow request: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a follow request already exists between two users
     */
    public boolean requestExists(int senderId, int receiverId) {
        String sql = "SELECT COUNT(*) FROM UserFollowers WHERE follower_id = ? AND followed_id = ? AND status = 'pending'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if request exists: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get pending follow requests for a user
     */
    public List<FollowRequest> getPendingRequests(int userId) {
        List<FollowRequest> requests = new ArrayList<>();
        String sql = """
            SELECT uf.id, uf.follower_id, uf.followed_id, uf.status, uf.created_at,
                   u.username, u.email, up.profile_picture
            FROM UserFollowers uf
            JOIN users u ON uf.follower_id = u.id
            JOIN user_profile up ON uf.followed_id = up.user_id
            WHERE uf.followed_id = ? AND uf.status = 'pending'
            ORDER BY uf.created_at DESC
        """;
        
        System.out.println("DEBUG DAO: Searching for pending requests for user ID: " + userId);
        System.out.println("DEBUG DAO: SQL Query: " + sql);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                FollowRequest request = new FollowRequest();
                request.setId(rs.getInt("id"));
                request.setSenderId(rs.getInt("follower_id"));
                request.setReceiverId(rs.getInt("followed_id"));
                request.setStatus(rs.getString("status"));
                request.setRequestDate(rs.getTimestamp("created_at").toLocalDateTime());
                request.setSenderUsername(rs.getString("username"));
                request.setSenderEmail(rs.getString("email"));
                request.setSenderProfilePicture(rs.getString("profile_picture"));
                requests.add(request);
                
                System.out.println("DEBUG DAO: Found request #" + count + " - ID: " + request.getId() + 
                                 ", From: " + request.getSenderUsername() + " (" + request.getSenderId() + ")" +
                                 ", To: " + request.getReceiverId() + ", Status: " + request.getStatus());
            }
            
            System.out.println("DEBUG DAO: Total requests found: " + requests.size());
            
        } catch (SQLException e) {
            System.err.println("Error getting pending requests: " + e.getMessage());
            e.printStackTrace();
        }
        
        return requests;
    }
    
    /**
     * Accept a follow request
     */
    public boolean acceptFollowRequest(int requestId) {
        String sql = "UPDATE UserFollowers SET status = 'accepted' WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error accepting follow request: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reject a follow request
     */
    public boolean rejectFollowRequest(int requestId) {
        String sql = "UPDATE UserFollowers SET status = 'rejected' WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error rejecting follow request: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user A is following user B
     */
    public boolean isFollowing(int followerId, int followedId) {
        String sql = "SELECT COUNT(*) FROM UserFollowers WHERE follower_id = ? AND followed_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if following: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get followers count for a user
     */
    public int getFollowersCount(int userId) {
        String sql = "SELECT COUNT(*) FROM UserFollowers WHERE followed_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting followers count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get following count for a user
     */
    public int getFollowingCount(int userId) {
        String sql = "SELECT COUNT(*) FROM UserFollowers WHERE follower_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting following count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Unfollow a user
     */
    public boolean unfollowUser(int followerId, int followedId) {
        String sql = "DELETE FROM UserFollowers WHERE follower_id = ? AND followed_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error unfollowing user: " + e.getMessage());
            return false;
        }
    }
}