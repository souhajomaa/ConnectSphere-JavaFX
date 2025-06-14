package com.example.projetjavafx.root.profile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileRepository {
    // Modèle (Model)
    private String username;
    private String email;
    private String bio;
    private String profilePicture;
    private int userId;

    public ProfileRepository(String username, String email, String bio, String profilePicture) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.userId = 0; // Valeur par défaut
    }

    public ProfileRepository(String username, String email, String bio, String profilePicture, int userId) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.userId = userId;
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

    public int getId() {
        return userId;
    }

    // Méthode pour établir la connexion à la base de données
    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://mysql-1dcac8df-moamedsalahsaoudi123-c05d.e.aivencloud.com:22451/defaultdb?ssl-mode=REQUIRED";
        String user = "avnadmin";
        String pass = "AVNS_5qB58jyOaJs3WW0eYS9";
        return DriverManager.getConnection(url, user, pass);
    }

    // Méthode pour récupérer les données de l'utilisateur
    public static ProfileRepository getUserProfile(int userId) {
        String query = "SELECT u.username, u.email, up.bio, up.profile_picture " +
                "FROM users u " +
                "LEFT JOIN user_profile up ON u.id = up.user_id " +
                "WHERE u.id = ?";

        try (Connection conn = new ProfileRepository(null, null, null, null).connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Assurez-vous que l'ID utilisateur est correctement défini
                ProfileRepository profile = new ProfileRepository(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("bio"),
                        rs.getString("profile_picture"),
                        userId  // Passer explicitement l'ID utilisateur
                );
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error while retrieving user data: " + e.getMessage());
        }
        return null;
    }
}