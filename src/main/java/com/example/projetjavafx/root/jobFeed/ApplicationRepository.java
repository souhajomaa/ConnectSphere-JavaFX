package com.example.projetjavafx.root.jobFeed;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.jobApplications.Application; // FIXED IMPORT

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ApplicationRepository {

    // Save a new application to the database
    public static void saveApplication(int userId, int jobId, String coverLetter, String resumePath)
            throws SQLException {
        String sql = "INSERT INTO applications (user_id_id, job_id_id, status, applied_at, rewarded, cover_letter, resume_path) " +
                "VALUES (?, ?, 'pending', ?, 0, ?, ?)";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Format the current date as a string
            String appliedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Set parameters
            pstmt.setInt(1, userId);
            pstmt.setInt(2, jobId);
            pstmt.setString(3, appliedAt);
            pstmt.setString(4, coverLetter);
            pstmt.setString(5, resumePath);

            // Execute the query
            pstmt.executeUpdate();
        }
    }

    // Retrieve all applications for a specific job
    public static List<Application> getApplicationsByJobId(int jobId) throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE job_id_id = ?";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                applications.add(new Application(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getString("status"),
                        rs.getString("applied_at"),
                        rs.getInt("rewarded"),
                        rs.getInt("job_id_id"),
                        rs.getString("cover_letter"),
                        rs.getString("resume_path")
                ));
            }
        }
        return applications;
    }

    public static void updateApplicationStatus(int applicationId, String status) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, applicationId);
            pstmt.executeUpdate();
        }
    }
}
