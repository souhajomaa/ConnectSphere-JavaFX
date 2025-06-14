package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.profile.ProfileRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserSearchRepository {
    private Connection connection;

    public UserSearchRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Recherche des utilisateurs par nom d'utilisateur ou email
     * @param query Le terme de recherche
     * @return Liste des profils d'utilisateurs correspondants
     * @throws SQLException En cas d'erreur de base de données
     */
    public List<ProfileRepository> searchUsers(String query) throws SQLException {
        List<ProfileRepository> users = new ArrayList<>();

        // Prépare la requête SQL pour rechercher par nom d'utilisateur ou email
        String sql = "SELECT u.id, u.username, u.email, up.bio, up.profile_picture " +
                     "FROM users u " +
                     "LEFT JOIN user_profile up ON u.id = up.user_id " +
                     "WHERE u.username LIKE ? OR u.email LIKE ? LIMIT 20";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Configure les paramètres de recherche avec des jokers
            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);

            // Exécute la requête
            ResultSet rs = stmt.executeQuery();

            // Traite les résultats
            while (rs.next()) {
                int userId = rs.getInt("id");
                // Créer directement l'objet ProfileRepository avec l'ID utilisateur
                ProfileRepository profile = new ProfileRepository(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("bio"),
                    rs.getString("profile_picture"),
                    userId  // Passer explicitement l'ID utilisateur
                );
                users.add(profile);
            }
        }

        return users;
    }
}