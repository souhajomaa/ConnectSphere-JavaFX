package com.example.projetjavafx.root.jobApplications;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.events.Event;
import com.example.projetjavafx.root.organizer.Job;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppliedJobsRepository {
    // Existing job-related method
    public static List<Job> getJobsAppliedByUser(int userId) throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        String sql = "SELECT j.id, j.job_title, j.event_title, j.job_location, j.employment_type, " +
                "j.application_dead_line, j.min_salary, j.max_salary, j.currency, j.job_descreption, " +
                "j.recruiter_name, j.recruiter_email, j.created_at " +
                "FROM jobs j JOIN applications a ON j.job_id = a.job_id_id " +
                "WHERE a.user_id_id = ?";

        List<Job> jobs = new ArrayList<>();
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                jobs.add(new Job(
                        rs.getInt("id"),
                        rs.getString("job_title"),
                        rs.getString("event_title"),
                        rs.getString("job_location"),
                        rs.getString("employment_type"),
                        rs.getString("application_dead_line"),
                        rs.getDouble("min_salary"),
                        rs.getDouble("max_salary"),
                        rs.getString("currency"),
                        rs.getString("job_descreption"),
                        rs.getString("recruiter_name"),
                        rs.getString("recruiter_email"),
                        rs.getString("created_at")
                ));
            }
        }
        return jobs;
    }

    // Add this missing method
    public static String getApplicationStatusForJob(int userId, int jobId) throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        String sql = "SELECT status FROM applications WHERE user_id_id = ? AND job_id_id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, jobId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }
            return "Unknown";
        }
    }

    // Participation-related methods
    public static List<Event> getParticipatedEvents() throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        String sql = "SELECT e.id, e.name, e.location, e.start_time, e.end_time " +
                "FROM events e JOIN participation_events p ON e.id = p.events_id " +
                "WHERE p.participation_id = ?";

        List<Event> events = new ArrayList<>();
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getInt("id"));
                event.setName(rs.getString("name"));
                event.setLocation(rs.getString("location"));
                event.setStartDate(rs.getString("start_time"));
                event.setEndDate(rs.getString("end_time"));
                events.add(event);
            }
        }
        return events;
    }

    public static boolean cancelParticipation(int eventId) throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        String sql = "DELETE FROM participation_events WHERE events_id = ? AND participation_id = ?";
        try (Connection conn = AivenMySQLManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, currentUserId);
            return pstmt.executeUpdate() > 0;
        }
    }
}