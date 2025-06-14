package com.example.projetjavafx.root.organizer;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
// import com.mysql.cj.Session; // Cet import semble inutile ici

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JobRepository {
    public static void createJob(String jobTitle , String eventTitle ,String jobLocation,String employmentType,String applicationDeadline,String minSalary,String maxSalary,String currency,String jobDescription , String recruiterName , String recruiterEmail) throws SQLException {

        Connection conn = null; // Initialiser à null
        PreparedStatement pstmt = null; // Initialiser à null

        // CORRECTION: Utiliser les noms de colonnes exacts de la table 'jobs'
        String sql = "INSERT INTO jobs (job_title, event_title, job_location, employment_type, application_dead_line, min_salary, max_salary, currency, job_descreption, recruiter_name, recruiter_email, user_id_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        //                                                                                 ^ ici                        ^ ici                                                   ^ ici

        try {
            conn = AivenMySQLManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            // Set parameters
            pstmt.setString(1, jobTitle);
            pstmt.setString(2, eventTitle);
            pstmt.setString(3, jobLocation);
            pstmt.setString(4, employmentType);
            pstmt.setString(5, applicationDeadline); // La valeur Java est correcte, la colonne SQL est corrigée dans la requête
            pstmt.setDouble(6, Double.parseDouble(minSalary));
            pstmt.setDouble(7, Double.parseDouble(maxSalary));
            pstmt.setString(8, currency);
            pstmt.setString(9, jobDescription);   // La valeur Java est correcte, la colonne SQL est corrigée dans la requête
            pstmt.setString(10, recruiterName);
            pstmt.setString(11, recruiterEmail);
            pstmt.setInt(12, SessionManager.getInstance().getCurrentUserId()); // Assurez-vous que user_id_id est le nom correct de la FK

            // Execute the query
            pstmt.executeUpdate();

        } finally {
            // Fermer les ressources dans un bloc finally pour garantir leur fermeture même en cas d'erreur
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace(); // Log l'erreur de fermeture
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace(); // Log l'erreur de fermeture
                }
            }
        }
    }
}