package com.example.projetjavafx.root.organizer;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizerRepository {

    // Fetch all events for the organizer
    public static List<Map<String, String>> getOrganizerEvents(int userId) throws SQLException {
        List<Map<String, String>> events = new ArrayList<>();
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();
        // CORRIGÉ: Utilise 'events' (minuscule) et 'id' comme PK, 'organizer_id_id' comme FK
        String sql = "SELECT id, name, description, start_time, end_time, location FROM events WHERE organizer_id_id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> event = new HashMap<>();
                event.put("event_id", rs.getString("id")); // Garde la clé 'event_id' pour compatibilité
                event.put("name", rs.getString("name"));
                event.put("description", rs.getString("description"));
                event.put("start_time", rs.getString("start_time"));
                event.put("end_time", rs.getString("end_time"));
                event.put("location", rs.getString("location"));
                events.add(event);
            }
        }
        return events;
    }

    // Fetch all jobs for the organizer
    public static List<Map<String, String>> getOrganizerJobs(int userId) throws SQLException {
        List<Map<String, String>> jobs = new ArrayList<>();
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();
        // CORRIGÉ: Utilise les noms de colonnes exacts: 'id', 'application_dead_line', 'job_descreption', 'user_id_id'
        String sql = "SELECT id, job_title, event_title, job_location, employment_type, application_dead_line, " +
                "min_salary, max_salary, currency, job_descreption, recruiter_name, recruiter_email, created_at " +
                "FROM jobs WHERE user_id_id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> job = new HashMap<>();
                job.put("job_id", rs.getString("id")); // Utilise 'id' de la table jobs
                job.put("job_title", rs.getString("job_title"));
                job.put("event_title", rs.getString("event_title"));
                job.put("job_location", rs.getString("job_location"));
                job.put("employment_type", rs.getString("employment_type"));
                job.put("application_deadline", rs.getString("application_dead_line")); // Lire la colonne correcte
                job.put("min_salary", rs.getString("min_salary"));
                job.put("max_salary", rs.getString("max_salary"));
                job.put("currency", rs.getString("currency"));
                job.put("job_description", rs.getString("job_descreption")); // Lire la colonne correcte
                job.put("recruiter_name", rs.getString("recruiter_name"));
                job.put("recruiter_email", rs.getString("recruiter_email"));
                job.put("created_at", rs.getString("created_at"));
                jobs.add(job);
            }
        }
        return jobs;
    }

    // Delete participations for a given event
    public static void deleteParticipationsForEvent(int eventId) throws SQLException {
        // CORRIGÉ: Utilise 'event_id' (supposant que c'est le nom dans la table participation)
        String sql = "DELETE FROM participation WHERE event_id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.executeUpdate();
        }
    }

    // Delete an event
    public static boolean deleteOrganizerEvent(int userId, int eventId) throws SQLException {
        deleteParticipationsForEvent(eventId);
        // CORRIGÉ: Utilise 'events' et 'id', 'organizer_id_id'
        String sql = "DELETE FROM events WHERE organizer_id_id = ? AND id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, eventId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Delete a job
    public static boolean deleteOrganizerJob(int userId, String jobTitle) throws SQLException {
        // CORRIGÉ: Utilise 'user_id_id'
        String sql = "DELETE FROM jobs WHERE user_id_id = ? AND job_title = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, jobTitle);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Update an event
    public static boolean updateOrganizerEvent(int userId, int eventId, String name, String description, String start_time, String end_time, String location) throws SQLException {
        // CORRIGÉ: Utilise 'events' et 'id', 'organizer_id_id'
        String sql = "UPDATE events SET name = ?, description = ?, start_time = ?, end_time = ?, location = ? WHERE id = ? AND organizer_id_id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, start_time);
            pstmt.setString(4, end_time);
            pstmt.setString(5, location);
            pstmt.setInt(6, eventId);
            pstmt.setInt(7, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Update a job
    public static boolean updateOrganizerJob(int userId, String originalJobTitle, String newJobTitle, String eventTitle, String jobLocation,
                                             String employmentType, String applicationDeadline, String minSalary, String maxSalary,
                                             String currency, String jobDescription, String recruiterName, String recruiterEmail) throws SQLException {
        // CORRIGÉ: Utilise les noms de colonnes corrects et 'user_id_id'
        String sql = "UPDATE jobs SET job_title = ?, event_title = ?, job_location = ?, employment_type = ?, application_dead_line = ?, "
                + "min_salary = ?, max_salary = ?, currency = ?, job_descreption = ?, recruiter_name = ?, recruiter_email = ? "
                + "WHERE user_id_id = ? AND job_title = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newJobTitle);
            pstmt.setString(2, eventTitle);
            pstmt.setString(3, jobLocation);
            pstmt.setString(4, employmentType);
            pstmt.setString(5, applicationDeadline);
            pstmt.setString(6, minSalary);
            pstmt.setString(7, maxSalary);
            pstmt.setString(8, currency);
            pstmt.setString(9, jobDescription);
            pstmt.setString(10, recruiterName);
            pstmt.setString(11, recruiterEmail);
            pstmt.setInt(12, userId);
            pstmt.setString(13, originalJobTitle);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Fetch participants for a specific event
    public static List<Map<String, String>> getParticipantsForEvent(int eventId) throws SQLException {
        List<Map<String, String>> participants = new ArrayList<>();
        // CORRIGÉ: Assurez-vous que la table s'appelle 'participation' et les colonnes 'participant_id', 'event_id', 'user_id'
        String sql = "SELECT u.username AS name, u.age, u.gender " +
                "FROM participation p " +
                "JOIN users u ON p.participant_id = u.user_id " +
                "WHERE p.event_id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> participant = new HashMap<>();
                participant.put("name", rs.getString("name"));
                participant.put("age", rs.getString("age"));
                participant.put("gender", rs.getString("gender"));
                participants.add(participant);
            }
        }
        return participants;
    }

    // Delete a participant from an event
    public static boolean deleteParticipant(int eventId, String participantName) throws SQLException {
        // CORRIGÉ: Assurez-vous que les tables/colonnes sont correctes
        String sql = "DELETE FROM participation WHERE event_id = ? AND participant_id = (SELECT user_id FROM users WHERE username = ?)";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setString(2, participantName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}