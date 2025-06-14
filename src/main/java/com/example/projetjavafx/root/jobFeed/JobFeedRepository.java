package com.example.projetjavafx.root.jobFeed;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.organizer.Job;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JobFeedRepository {

    // Méthode applyForJob (inchangée)
    public static void applyForJob(int currentUserId, int jobId, String coverLetter, String resumePath) throws SQLException {
        String sql = "INSERT INTO applications (user_id_id, job_id_id, status, applied_at, rewarded, cover_letter, resume_path) " +
                "VALUES (?, ?, 'pending', ?, 0, ?, ?)";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, jobId);
            pstmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(4, coverLetter);
            pstmt.setString(5, resumePath);

            pstmt.executeUpdate();
        }
    }

    // Méthode getAllJobs (MODIFIÉE)
    public static List<Job> getAllJobs() throws SQLException {
        List<Job> jobs = new ArrayList<>();
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        // CORRIGÉ: Utilise les noms de colonnes exacts: application_dead_line, job_descreption, user_id_id
        String sql = "SELECT id, job_title, event_title, job_location, employment_type, application_dead_line, " +
                "min_salary, max_salary, currency, job_descreption, recruiter_name, recruiter_email, created_at " +
                "FROM jobs WHERE user_id_id != ? ORDER BY created_at DESC" ;

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))  {

            pstmt.setInt(1, currentUserId); // Exclure les jobs de l'utilisateur actuel

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Job job = new Job(
                        rs.getInt("id"), // Lire la colonne 'id' de la table jobs
                        rs.getString("job_title"),
                        rs.getString("event_title"),
                        rs.getString("job_location"),
                        rs.getString("employment_type"),
                        rs.getString("application_dead_line"), // Lire la colonne correcte
                        rs.getDouble("min_salary"),
                        rs.getDouble("max_salary"),
                        rs.getString("currency"),
                        rs.getString("job_descreption"), // CORRIGÉ: Lire 'job_descreption'
                        rs.getString("recruiter_name"),
                        rs.getString("recruiter_email"),
                        rs.getString("created_at")
                );
                jobs.add(job);
            }
        }
        return jobs;
    }
}