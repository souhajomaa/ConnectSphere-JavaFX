package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.group.GroupProfileRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupSearchRepository {
    private Connection connection;

    public GroupSearchRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Recherche des groupes par nom ou description
     * @param query Le terme de recherche
     * @return Liste des profils de groupes correspondants
     * @throws SQLException En cas d'erreur de base de données
     */
    public List<GroupProfileRepository> searchGroups(String query) throws SQLException {
        List<GroupProfileRepository> groups = new ArrayList<>();
        
        // Prépare la requête SQL pour rechercher par nom ou description
        String sql = "SELECT name, description, rules, profile_picture FROM user_groups WHERE name LIKE ? OR description LIKE ? LIMIT 20";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Configure les paramètres de recherche avec des jokers
            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            
            // Exécute la requête
            ResultSet rs = stmt.executeQuery();
            
            // Traite les résultats
            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                String rules = rs.getString("rules");
                String profilePicture = rs.getString("profile_picture");
                
                GroupProfileRepository group = new GroupProfileRepository(name, description, rules, profilePicture);
                groups.add(group);
            }
        }
        
        return groups;
    }
}