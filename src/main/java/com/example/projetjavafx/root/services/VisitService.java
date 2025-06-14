package com.example.projetjavafx.root.services;

import com.example.projetjavafx.root.models.Point;
import com.example.projetjavafx.root.services.DatabaseService;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class VisitService {

    private final PointsService pointsService = new PointsService();

    // Vérifier si une entrée existe pour l'utilisateur, sinon en créer une
    private void ensureVisitEntryExists(int idUser) {
        Connection conn = null;
        try {
            conn = DatabaseService.getConnection();
            conn.setAutoCommit(false); // Désactiver l'auto-commit pour gérer la transaction manuellement

            // Vérifier si une entrée existe
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM visite_utilisateur WHERE user_id = ?")) {
                stmt.setInt(1, idUser);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Nombre d'entrées pour l'utilisateur " + idUser + " : " + count);
                    if (count == 0) {
                        // Aucune entrée n'existe, en créer une avec la date actuelle
                        try (PreparedStatement insertStmt = conn.prepareStatement(
                                "INSERT INTO visite_utilisateur (user_id, serie, dernier_visite) VALUES (?, 0, ?)")) {
                            insertStmt.setInt(1, idUser);
                            insertStmt.setDate(2, Date.valueOf(LocalDate.now())); // Définir la date actuelle
                            int rowsInserted = insertStmt.executeUpdate();
                            System.out.println("Nouvelle entrée créée pour l'utilisateur " + idUser + " dans visite_utilisateur, lignes insérées : " + rowsInserted);
                        }
                    } else {
                        System.out.println("Entrée déjà existante pour l'utilisateur " + idUser + " dans visite_utilisateur");
                    }
                }
            }

            // Valider la transaction
            conn.commit();
            System.out.println("Transaction validée pour l'utilisateur " + idUser);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification/création de l'entrée pour l'utilisateur " + idUser + " : " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction annulée pour l'utilisateur " + idUser);
                } catch (SQLException rollbackEx) {
                    System.err.println("Erreur lors de l'annulation de la transaction : " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Réactiver l'auto-commit
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Erreur lors de la fermeture de la connexion : " + closeEx.getMessage());
                    closeEx.printStackTrace();
                }
            }
        }
    }

    public int getStreak(int idUser) {
        int streak = 0;
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT serie FROM visite_utilisateur WHERE user_id = ?")) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                streak = rs.getInt("Serie");
                System.out.println("Streak récupéré pour l'utilisateur " + idUser + " : " + streak);
            } else {
                System.out.println("Aucune entrée trouvée pour l'utilisateur " + idUser + " dans visite_utilisateur");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du streak pour l'utilisateur " + idUser + " : " + e.getMessage());
            e.printStackTrace();
        }
        return streak;
    }

    public Date getLastVisit(int idUser) {
        Date lastDate = null;
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT dernier_visite FROM visite_utilisateur WHERE user_id = ?")) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lastDate = rs.getDate("dernier_visite");
                System.out.println("Dernière visite pour l'utilisateur " + idUser + " : " + (lastDate != null ? lastDate : "null"));
            } else {
                System.out.println("Aucune dernière visite trouvée pour l'utilisateur " + idUser + " dans visite_utilisateur");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la dernière visite pour l'utilisateur " + idUser + " : " + e.getMessage());
            e.printStackTrace();
        }
        return lastDate;
    }

    public void updateStreak(int idUser) {
        ensureVisitEntryExists(idUser); // S'assurer qu'une entrée existe

        LocalDate today = LocalDate.now();
        Date lastVisitDate = getLastVisit(idUser);
        int currentStreak = getStreak(idUser);
        int newStreak = currentStreak;

        System.out.println("Mise à jour du streak pour l'utilisateur " + idUser);
        System.out.println("Aujourd'hui : " + today);
        System.out.println("Dernière visite (Date) : " + (lastVisitDate != null ? lastVisitDate : "null"));
        System.out.println("Streak actuel : " + currentStreak);

        if (lastVisitDate == null) {
            // Première visite de l'utilisateur
            newStreak = 1;
            System.out.println("Première visite détectée, newStreak = " + newStreak);
        } else {
            // Convertir java.sql.Date en LocalDate
            LocalDate lastVisit = lastVisitDate.toLocalDate();
            LocalDate yesterday = today.minusDays(1);

            System.out.println("Dernière visite (LocalDate) : " + lastVisit);
            System.out.println("Hier : " + yesterday);

            // Vérifier l'inactivité
            long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisit, today);
            if (daysSinceLastVisit >= 14) {
                // Calculer le nombre d'intervalles de 14 jours
                int intervals = (int) (daysSinceLastVisit / 14);
                int pointsToDeduct = intervals * 5; // 5 points par intervalle de 14 jours

                System.out.println("Inactivité de " + daysSinceLastVisit + " jours détectée, " + intervals + " intervalles de 14 jours, déduction de " + pointsToDeduct + " points");

                // Appliquer la déduction
                if (pointsToDeduct > 0) {
                    int currentPoints = pointsService.getUserPoints(idUser);
                    int newPoints = Math.max(0, currentPoints - pointsToDeduct); // Ne pas descendre en dessous de 0
                    pointsService.updateUserPoints(idUser, newPoints);

                    // Ajouter une entrée dans l'historique pour chaque intervalle
                    for (int i = 1; i <= intervals; i++) {
                        long intervalDays = i * 14;
                        Point penaltyPoint = new Point("perte", 5, "absence de " + intervalDays + " jour", LocalDate.now());
                        pointsService.addPoints(idUser, penaltyPoint);
                    }

                    // Afficher un message de confirmation pour la perte de points
                    showConfirmationDialog("Inactivité détectée", "  bienvenu encore une fois malheureusement! Tu as perdu " + pointsToDeduct + " points pour " + daysSinceLastVisit + " jours d'absence.");
                }

                // Réinitialiser le streak
                newStreak = 1;
                System.out.println("Streak réinitialisé après inactivité, newStreak = " + newStreak);
            } else if (lastVisit.equals(yesterday)) {
                // Visite consécutive : incrémenter le streak
                newStreak = currentStreak + 1;
                System.out.println("Visite consécutive détectée, newStreak = " + newStreak);
            } else if (!lastVisit.equals(today)) {
                // Pas une visite consécutive et pas aujourd'hui : réinitialiser le streak
                newStreak = 1;
                System.out.println("Visite non consécutive détectée, newStreak = " + newStreak);
            } else {
                System.out.println("Visite aujourd'hui déjà détectée, pas de changement de streak");
            }
        }

        // Si le streak atteint 7 jours, ajouter des points et réinitialiser le streak
        if (newStreak == 7) {
            int currentPoints = pointsService.getUserPoints(idUser);
            int bonusPoints = 50; // 50 points pour 7 jours consécutifs
            pointsService.updateUserPoints(idUser, currentPoints + bonusPoints);

            // Ajouter une entrée dans l'historique des points
            Point bonusPoint = new Point("gain", bonusPoints, "visite 7 jour", LocalDate.now());
            pointsService.addPoints(idUser, bonusPoint);

            // Afficher un message de confirmation pour le gain de points
            showConfirmationDialog("Félicitations !", "Tu nous as visité 7 jours consécutifs ! Tu as gagné " + bonusPoints + " points.");

            // Réinitialiser le streak
            newStreak = 0;
            System.out.println("Streak de 7 jours atteint, points ajoutés, newStreak réinitialisé à " + newStreak);
        }

        // Mettre à jour le streak et la date de la dernière visite
        Connection conn = null;
        try {
            conn = DatabaseService.getConnection();
            conn.setAutoCommit(false); // Désactiver l'auto-commit pour gérer la transaction manuellement

            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE visite_utilisateur SET dernier_visite = ?, serie = ? WHERE user_id = ?")) {
                stmt.setDate(1, Date.valueOf(today)); // Utiliser LocalDate.now() pour la date
                stmt.setInt(2, newStreak);
                stmt.setInt(3, idUser);
                int rowsUpdated = stmt.executeUpdate();
                System.out.println("Nombre de lignes mises à jour : " + rowsUpdated);
                System.out.println("Base de données mise à jour : Serie = " + newStreak + ", dernier_visite = " + today + " pour l'utilisateur " + idUser);
            }

            // Valider la transaction
            conn.commit();
            System.out.println("Transaction validée pour la mise à jour du streak de l'utilisateur " + idUser);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du streak pour l'utilisateur " + idUser + " : " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction annulée pour la mise à jour du streak de l'utilisateur " + idUser);
                } catch (SQLException rollbackEx) {
                    System.err.println("Erreur lors de l'annulation de la transaction : " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Réactiver l'auto-commit
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Erreur lors de la fermeture de la connexion : " + closeEx.getMessage());
                    closeEx.printStackTrace();
                }
            }
        }

        // Vérifier la valeur après mise à jour
        int streakAfterUpdate = getStreak(idUser);
        System.out.println("Streak après mise à jour (vérification) : " + streakAfterUpdate);
    }

    // Méthode pour afficher une boîte de dialogue de confirmation
    private void showConfirmationDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}