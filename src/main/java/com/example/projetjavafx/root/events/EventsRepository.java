package com.example.projetjavafx.root.events;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class EventsRepository {

    // getCategories() method remains the same...
    public static ObservableList<String> getCategories() {
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("All"); // Option par défaut
        String sql = "SELECT id, name FROM category"; // Assurez-vous que le nom de la table est correct
        try (Connection connection = AivenMySQLManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {
            while (resultSet.next()) {
                int categoryId = resultSet.getInt("id");
                String categoryName = resultSet.getString("name");
                categories.add(categoryId + " - " + categoryName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }


    // participateInEvent() method remains the same...
    public static void participateInEvent(int eventId) {
        int currentUser = SessionManager.getInstance().getCurrentUserId();

        // Vérification si l'utilisateur est connecté
        if (currentUser == -1) {
            System.out.println("Aucun utilisateur connecté, veullez connecter ou créer un compte !");
            return;
        }

        try (Connection connection = AivenMySQLManager.getConnection()) {

            // Vérification si l'utilisateur existe dans la table Users
            // Assurez-vous que la colonne ID dans la table Users est bien 'user_id'
            String userCheckQuery = "SELECT COUNT(*) FROM users WHERE user_id = ?"; // Changé de 'id' à 'user_id'
            try (PreparedStatement userCheckStmt = connection.prepareStatement(userCheckQuery)) {
                userCheckStmt.setInt(1, currentUser);
                ResultSet rs = userCheckStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Erreur : L'utilisateur avec l'ID " + currentUser + " n'existe pas !");
                    return;
                }
            }

            // Vérification si l'utilisateur est déjà inscrit à l'événement
            // Assurez-vous que les noms de table/colonnes sont corrects : participation, event_id, participant_id
            String checkParticipationQuery = "SELECT COUNT(*) FROM participation WHERE event_id = ? AND participant_id = ?"; // Changé de participation_events à participation
            try (PreparedStatement checkStmt = connection.prepareStatement(checkParticipationQuery)) {
                checkStmt.setInt(1, eventId);
                checkStmt.setInt(2, currentUser);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("L'utilisateur participe déjà à cet événement.");
                    return;
                }
            }

            // Insérer l'utilisateur dans la table participation
            String sql = "INSERT INTO participation (event_id, participant_id) VALUES (?, ?)"; // Changé de participation_events à participation
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, eventId);
                statement.setInt(2, currentUser);
                statement.executeUpdate();
                System.out.println("Participation enregistrée avec succès !");
                SessionManager.getInstance().updatepart(); // Assurez-vous que cette méthode existe et fait ce qu'il faut
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupère la liste des événements en fonction d'un texte de recherche et d'un filtre de catégorie
    public static ObservableList<Event> getEvents(String searchText, String category) {
        ObservableList<Event> events = FXCollections.observableArrayList();
        String query = "SELECT e.*, c.name AS category_name " +
                "FROM events e " +
                "LEFT JOIN category c ON e.category_id_id = c.id " + // Jointure correcte
                "WHERE 1=1 ";

        if (searchText != null && !searchText.isEmpty()) {
            query += "AND (LOWER(e.name) LIKE ? OR LOWER(e.description) LIKE ?) ";
        }
        if (category != null && !category.equals("All")) {
            // Filter by category ID
            try {
                int categoryId = Integer.parseInt(category.split(" - ")[0]);
                query += "AND e.category_id_id = ? ";
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Invalid category format: " + category + ". Ignoring category filter.");
            }
        }

        // ----- CORRECTION DE LA CLAUSE GROUP BY -----
        // Utilisez 'e.id' (clé primaire de events) au lieu de 'e.event_id'
        // Listez explicitement les colonnes de 'e' ou assurez-vous que votre SGBD gère SELECT * avec GROUP BY id
        query += " GROUP BY e.id, e.organizer_id_id, e.category_id_id, e.name, e.description, e.start_time, e.end_time, e.location, e.image, e.points, c.name";
        // -------------------------------------------

        try (Connection connection = AivenMySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            int paramIndex = 1;
            if (searchText != null && !searchText.isEmpty()) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                statement.setString(paramIndex++, searchPattern);
                statement.setString(paramIndex++, searchPattern);
            }
            if (category != null && !category.equals("All")) {
                try {
                    int categoryId = Integer.parseInt(category.split(" - ")[0]);
                    statement.setInt(paramIndex++, categoryId);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Ne rien faire si le format est invalide, le paramètre ne sera pas ajouté
                }
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = new Event();
                event.setEventId(resultSet.getInt("id")); // Utilisez 'id' au lieu de 'event_id' pour correspondre au schéma
                event.setName(resultSet.getString("name"));
                event.setDescription(resultSet.getString("description"));
                event.setStartDate(resultSet.getString("start_time"));
                event.setEndDate(resultSet.getString("end_time"));
                event.setLocation(resultSet.getString("location"));
                event.setImageBase64(resultSet.getString("image"));
                event.setCategoryName(resultSet.getString("category_name"));

                if (SessionManager.getInstance().getCurrentUserId() != resultSet.getInt("organizer_id_id")) {
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getEvents: " + e.getMessage()); // Log plus précis
            e.printStackTrace(); // Imprime la trace complète
        }
        return events;
    }
}