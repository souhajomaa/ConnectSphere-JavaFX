package com.example.projetjavafx.root.jobApplications;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobApplicationsRepository {
    SessionManager sessionManager = SessionManager.getInstance();
    int currentUserId = sessionManager.getCurrentUserId();

    public static List<Application> getApplicationsForUserPostedJobs(int currentUserId) throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.*, j.job_description FROM applications a " +
                "INNER JOIN jobs j ON a.job_id_id = j.id " +
                "WHERE j.user_id_id = ?";

        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Application application = new Application(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getString("status"),
                        rs.getString("applied_at"),
                        rs.getInt("rewarded"),
                        rs.getInt("job_id_id"),
                        rs.getString("cover_letter"),
                        rs.getString("resume_path")
                );
                application.setCoverRating(rs.getInt("cover_rating")); // Load existing rating
                applications.add(application);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching applications: " + e.getMessage());
            throw e;
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

    public static void updateCoverRating(int applicationId, int rating) throws SQLException {
        String sql = "UPDATE applications SET cover_rating = ? WHERE id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating);
            pstmt.setInt(2, applicationId);
            pstmt.executeUpdate();
        }
    }

    public static String getJobDescription(int jobId) throws SQLException {
        String sql = "SELECT job_descreption FROM jobs WHERE id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jobId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("job_descreption") : "";
        }
    }
}