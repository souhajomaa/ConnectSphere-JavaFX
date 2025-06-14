package com.example.projetjavafx.root.profile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProfileRepository {
    // Model for user profile data
    private String username;
    private String email;
    private String bio;
    private String profilePicture;

    // Constructor
    public EditProfileRepository(String username, String email, String bio, String profilePicture) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profilePicture = profilePicture;
    }

    // Getters
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getBio() {
        return bio;
    }
    public String getProfilePicture() {
        return profilePicture;
    }

    // Establish the database connection
    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://mysql-1dcac8df-moamedsalahsaoudi123-c05d.e.aivencloud.com:22451/defaultdb?ssl-mode=REQUIRED";
        String user = "avnadmin";
        String pass = "AVNS_5qB58jyOaJs3WW0eYS9";
        return DriverManager.getConnection(url, user, pass);
    }

    // Load user data by user ID
    public static EditProfileRepository loadUserData(int userId) {
        String query = "SELECT u.username, u.email, up.bio, up.profile_picture " +
                "FROM users u " +
                "LEFT JOIN user_profile up ON u.id = up.user_id " +
                "WHERE u.id = ?";

        try (Connection conn = new EditProfileRepository(null, null, null, null).connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new EditProfileRepository(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("bio"),
                        rs.getString("profile_picture")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error while loading user data: " + e.getMessage());
        }
        return null;
    }

    // Update the user profile data
    public boolean updateProfile(int userId, String username, String email, String bio, String profilePicture) {
        // First check if a UserProfile record exists for this user
        String checkQuery = "SELECT COUNT(*) FROM user_profile WHERE user_id = ?";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                // No UserProfile record exists, create one
                String insertQuery = "INSERT INTO user_profile (user_id, bio, profile_picture) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, bio);
                    insertStmt.setString(3, profilePicture);
                    insertStmt.executeUpdate();
                    System.out.println("Created new UserProfile record for user ID: " + userId);
                }
            }

            // Now update both tables
            String updateUsersQuery = "UPDATE users SET username = ?, email = ? WHERE id = ?";
            String updateProfileQuery = "UPDATE user_profile SET bio = ?, profile_picture = ? WHERE user_id = ?";

            boolean usersUpdated = false;
            boolean profileUpdated = false;

            // Update Users table
            try (PreparedStatement updateUsersStmt = conn.prepareStatement(updateUsersQuery)) {
                updateUsersStmt.setString(1, username);
                updateUsersStmt.setString(2, email);
                updateUsersStmt.setInt(3, userId);
                usersUpdated = updateUsersStmt.executeUpdate() > 0;
            }

            // Update UserProfile table
            try (PreparedStatement updateProfileStmt = conn.prepareStatement(updateProfileQuery)) {
                updateProfileStmt.setString(1, bio);
                updateProfileStmt.setString(2, profilePicture);
                updateProfileStmt.setInt(3, userId);
                profileUpdated = updateProfileStmt.executeUpdate() > 0;
            }

            return usersUpdated || profileUpdated; // Return true if either update was successful

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error while updating profile: " + e.getMessage());
            return false;
        }
    }
}
