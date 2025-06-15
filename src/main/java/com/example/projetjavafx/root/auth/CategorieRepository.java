package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategorieRepository {

    private Connection connect() throws SQLException {
        return AivenMySQLManager.getConnection();
    }

    public int getCategoryId(String categoryName) throws SQLException {
        String query = "SELECT id FROM category WHERE name = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;  // Retourne -1 si la catégorie n'existe pas
    }

    public void insertUserInterest(int userId, int categoryId) throws SQLException {
        String checkCategorySql = "SELECT COUNT(*) FROM category WHERE id = ?";
        String insertSql = "INSERT INTO user_interests (user_id_id, category_id_id) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement checkCategoryStmt = conn.prepareStatement(checkCategorySql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Vérifie si la catégorie existe
            checkCategoryStmt.setInt(1, categoryId);
            ResultSet rs = checkCategoryStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Category ID " + categoryId + " does not exist.");
                return; // Ne pas insérer si la catégorie n'existe pas
            }

            // Insère l’intérêt de l’utilisateur
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, categoryId);
            insertStmt.executeUpdate();
        }
    }
}
