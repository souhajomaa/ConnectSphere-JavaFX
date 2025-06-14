package com.example.projetjavafx.root.messagerie.db;


import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.messagerie.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



public class MessageDB {
   // pour enregistrer les messages dans la base de données
    public boolean saveMessage(Message message) {
        String query = "INSERT INTO Messages (sender_id, recipient_id, content, timestamp, type, read_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getRecipientId());
            stmt.setString(3, message.getContent());
            stmt.setTimestamp(4, Timestamp.valueOf(message.getTimestamp()));
            stmt.setString(5, message.getType());

            // Set read_status to true if sender is the recipient (self-messages)
            // or if it's a system message
            boolean isRead = message.getSenderId() == message.getRecipientId() || "SYSTEM".equals(message.getType());
            stmt.setBoolean(6, isRead);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Message saved to database: " + (rowsAffected > 0));
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error saving message to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // pour récupérer l'historique des conversations
    public List<Message> getConversation(int user1Id, int user2Id) {
        List<Message> messages = new ArrayList<>();

        try (Connection conn = AivenMySQLManager.getConnection()) {
            // Vérifier d'abord la structure de la table
            String tableStructure = getTableStructure(conn);
            System.out.println("Table Messages structure: " + tableStructure);

            // Construire la requête en fonction des colonnes disponibles
            String query;
            if (tableStructure.contains("recipient_id") && tableStructure.contains("sender_id")) {
                query = "SELECT * FROM Messages WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?) ORDER BY timestamp ASC";
            } else if (tableStructure.contains("to_user") && tableStructure.contains("from_user")) {
                query = "SELECT * FROM Messages WHERE (from_user = ? AND to_user = ?) OR (from_user = ? AND to_user = ?) ORDER BY timestamp ASC";
            } else {
                System.err.println("Cannot determine appropriate columns for query");
                return messages;
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, user1Id);
                stmt.setInt(2, user2Id);
                stmt.setInt(3, user2Id);
                stmt.setInt(4, user1Id);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Message message = new Message();

                    // Essayer de lire le type, avec une valeur par défaut si la colonne n'existe pas
                    try {
                        message.setType(rs.getString("type"));
                    } catch (SQLException e) {
                        message.setType("MESSAGE");
                    }

                    // Lire sender_id ou from_user
                    try {
                        message.setSenderId(rs.getInt("sender_id"));
                    } catch (SQLException e) {
                        try {
                            message.setSenderId(rs.getInt("from_user"));
                        } catch (SQLException ex) {
                            System.err.println("Cannot find sender column");
                            continue;
                        }
                    }

                    // Lire recipient_id ou to_user
                    try {
                        message.setRecipientId(rs.getInt("recipient_id"));
                    } catch (SQLException e) {
                        try {
                            message.setRecipientId(rs.getInt("to_user"));
                        } catch (SQLException ex) {
                            System.err.println("Cannot find recipient column");
                            continue;
                        }
                    }

                    message.setContent(rs.getString("content"));

                    // Lire timestamp
                    try {
                        message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    } catch (SQLException e) {
                        message.setTimestamp(LocalDateTime.now());
                    }

                    messages.add(message);
                }

                System.out.println("Loaded " + messages.size() + " messages from database for conversation between users " + user1Id + " and " + user2Id);
            }
        } catch (SQLException e) {
            System.err.println("Error loading conversation from database: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }

    public boolean markMessagesAsRead(int userId, int otherUserId) {
        try (Connection conn = AivenMySQLManager.getConnection()) {
            String query = "UPDATE Messages SET read_status = TRUE WHERE sender_id = ? AND recipient_id = ? AND read_status = FALSE";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, otherUserId);
                stmt.setInt(2, userId);

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //methode poru creation de table Message avec verification
    public boolean createMessagesTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS Messages (" +
                "message_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "sender_id INT NOT NULL, " +
                "recipient_id INT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "type VARCHAR(20) DEFAULT 'MESSAGE', " +
                "read_status BOOLEAN DEFAULT FALSE)";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.executeUpdate();
            System.out.println("Messages table created or already exists");

            // Vérifier si les colonnes existent
            try {
                String checkQuery = "SELECT sender_id, recipient_id, read_status FROM Messages LIMIT 1";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.executeQuery();
                checkStmt.close();
                System.out.println("Table Messages structure is correct");
            } catch (SQLException e) {
                System.err.println("Table Messages exists but has incorrect structure. Attempting to alter table...");
                try {
                    // Essayer d'ajouter les colonnes manquantes
                    String alterQuery = "ALTER TABLE Messages " +
                            "ADD COLUMN IF NOT EXISTS sender_id INT NOT NULL, " +
                            "ADD COLUMN IF NOT EXISTS recipient_id INT NOT NULL, " +
                            "ADD COLUMN IF NOT EXISTS type VARCHAR(20) DEFAULT 'MESSAGE', " +
                            "ADD COLUMN IF NOT EXISTS read_status BOOLEAN DEFAULT FALSE";
                    PreparedStatement alterStmt = conn.prepareStatement(alterQuery);
                    alterStmt.executeUpdate();
                    alterStmt.close();
                    System.out.println("Table Messages structure updated successfully");
                } catch (SQLException ex) {
                    System.err.println("Failed to alter table Messages: " + ex.getMessage());
                    ex.printStackTrace();
                    return false;
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error creating Messages table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Ajouter une méthode pour obtenir la structure de la table
    private String getTableStructure(Connection conn) throws SQLException {
        StringBuilder structure = new StringBuilder();

        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "Messages", null)) {
            while (rs.next()) {
                structure.append(rs.getString("COLUMN_NAME")).append(", ");
            }
        }

        return structure.toString();
    }


    /**
     *
     * @param user1Id
     * @param user2Id
     * @return
     */
    // Améliorer la méthode getLastMessage pour gérer les erreurs
    public Message getLastMessage(int user1Id, int user2Id) {
        try (Connection conn = AivenMySQLManager.getConnection()) {
            String query = "SELECT * FROM Messages WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?) ORDER BY timestamp DESC LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, user1Id);
                stmt.setInt(2, user2Id);
                stmt.setInt(3, user2Id);
                stmt.setInt(4, user1Id);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Message message = new Message();

                    try {
                        message.setType(rs.getString("type"));
                    } catch (SQLException e) {
                        message.setType("MESSAGE");
                    }

                    try {
                        message.setSenderId(rs.getInt("sender_id"));
                    } catch (SQLException e) {
                        try {
                            message.setSenderId(rs.getInt("from_user"));
                        } catch (SQLException ex) {
                            System.err.println("Cannot find sender column");
                            return null;
                        }
                    }

                    try {
                        message.setRecipientId(rs.getInt("recipient_id"));
                    } catch (SQLException e) {
                        try {
                            message.setRecipientId(rs.getInt("to_user"));
                        } catch (SQLException ex) {
                            System.err.println("Cannot find recipient column");
                            return null;
                        }
                    }

                    message.setContent(rs.getString("content"));

                    try {
                        message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    } catch (SQLException e) {
                        message.setTimestamp(LocalDateTime.now());
                    }

                    return message;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting last message: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Améliorer la méthode getUnreadCount pour gérer les erreurs
    public int getUnreadCount(int userId, int otherUserId) {
        int count = 0;

        try (Connection conn = AivenMySQLManager.getConnection()) {
            // Vérifier si la colonne read_status existe
            boolean hasReadStatus = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "Messages", "read_status")) {
                hasReadStatus = rs.next();
            }

            if (!hasReadStatus) {
                System.out.println("read_status column does not exist, returning 0 unread messages");
                return 0;
            }

            String query = "SELECT COUNT(*) FROM Messages WHERE sender_id = ? AND recipient_id = ? AND read_status = FALSE";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, otherUserId);
                stmt.setInt(2, userId);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
        }

        return count;
    }
}