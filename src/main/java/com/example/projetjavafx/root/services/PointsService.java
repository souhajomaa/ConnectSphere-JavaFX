package com.example.projetjavafx.root.services;

import com.example.projetjavafx.root.models.Point;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointsService {

    public int getUserPoints(int userId) {
        int points = 0;
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT points FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                points = rs.getInt("points");
            } else {
                // Si l'utilisateur n'existe pas, on peut initialiser ses points à 0
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (id, points, argent) VALUES (?, 0, 0)")) {
                    insertStmt.setInt(1, userId);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des points pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
        return points;
    }

    public void updateUserPoints(int userId, int newPoints) {
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET points = ? WHERE id = ?")) {
            stmt.setInt(1, newPoints);
            stmt.setInt(2, userId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                // Si aucune ligne n'a été mise à jour, l'utilisateur n'existe pas
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (id, points, argent) VALUES (?, ?, 0)")) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, newPoints);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des points pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addPoints(int userId, Point points) {
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO historique_points (user_id_id, type, points, raison, date) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, userId);
            stmt.setString(2, points.getType());
            stmt.setInt(3, points.getPoints());
            stmt.setString(4, points.getRaison());
            stmt.setDate(5, java.sql.Date.valueOf(points.getTimestamp()));
            int rowsInserted = stmt.executeUpdate();
            System.out.println("Entrée ajoutée dans historique_points pour l'utilisateur " + userId + " : " + rowsInserted + " ligne(s) insérée(s)");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout des points dans l'historique pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Point> getPointsLog(int userId) {
        List<Point> pointsList = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM historique_points WHERE user_id_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                int points = rs.getInt("points");
                String raison = rs.getString("raison");
                LocalDate date = rs.getDate("date").toLocalDate();
                Point point = new Point(type, points, raison, date);
                pointsList.add(point);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique des points pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
        return pointsList;
    }

    public List<Point> getPointsLogByType(int userId, String type_) {
        List<Point> pointsList = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM historique_points WHERE user_id_id = ? AND type LIKE ?")) {
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + type_ + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                int points = rs.getInt("points");
                String raison = rs.getString("raison");
                LocalDate date = rs.getDate("date").toLocalDate();
                Point point = new Point(type, points, raison, date);
                pointsList.add(point);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique des points par type pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
        return pointsList;
    }

    public List<Point> getPointsLogByRaison(int userId, String raison_) {
        List<Point> pointsList = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM historique_points WHERE user_id_id = ? AND raison LIKE ?")) {
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + raison_ + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                int points = rs.getInt("points");
                String raison = rs.getString("raison");
                LocalDate date = rs.getDate("date").toLocalDate();
                Point point = new Point(type, points, raison, date);
                pointsList.add(point);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique des points par raison pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
        return pointsList;
    }

    public Map<String, Integer> getPointsHistoryByDay(int userId, String type) {
        Map<String, Integer> pointsList = new HashMap<>();
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DATE_FORMAT(date, '%Y/%m/%d') as date_, SUM(points) as sumPoints " +
                             "FROM historique_points " +
                             "WHERE user_id_id = ? " +
                             "AND WEEK(date) = WEEK(CURDATE()) " +
                             "AND YEAR(date) = YEAR(CURDATE()) " +
                             "AND type = ? " +
                             "GROUP BY date_")) {
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int points = rs.getInt("sumPoints");
                String date = rs.getString("date_");
                pointsList.put(date, points);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique des points par jour pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
        return pointsList;
    }

    // Méthodes conservées pour la logique de conversion
    public double getUserMoney(int userId) {
        double argent = 0;
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT argent FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                argent = rs.getDouble("argent");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'argent pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
        return argent;
    }

    public void updateUserArgent(int userId, double argent) {
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET argent = ? WHERE id = ?")) {
            stmt.setDouble(1, argent);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'argent pour l'utilisateur " + userId + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}