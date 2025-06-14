package com.example.projetjavafx.root.services;

import com.example.projetjavafx.root.models.HistoriqueTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    public List<HistoriqueTransaction> getTransactionLog(int userId) {
        List<HistoriqueTransaction> transactionList = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transaction_argent WHERE user_id = ?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Double montant = rs.getDouble("montant");
                int pointConvertis = rs.getInt("point_convertis");
                String devise = rs.getString("devise");
                Date date = rs.getDate("date");
                HistoriqueTransaction transaction = new HistoriqueTransaction(date, pointConvertis, devise, montant);
                transactionList.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionList;
    }

    public List<HistoriqueTransaction> getTransactionLogByDevise(int userId,String devise_) {
        List<HistoriqueTransaction> transactionList = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transaction_argent WHERE user_id = ? and devise like ?")) {

            stmt.setInt(1, userId);
            stmt.setString(2, "%"+devise_+"%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Double montant = rs.getDouble("montant");
                int pointConvertis = rs.getInt("point_convertis");
                String devise = rs.getString("devise");
                Date date = rs.getDate("date");
                HistoriqueTransaction transaction = new HistoriqueTransaction(date, pointConvertis, devise, montant);
                transactionList.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionList;
    }

    public void add(HistoriqueTransaction transaction, int userId) {
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO transaction_argent (user_id, type, montant, devise, point_convertis) " +
                             "VALUES (?, ?, ?, ?, ?)")) {

            stmt.setInt(1, userId);
            stmt.setString(2, transaction.getType());
            stmt.setDouble(3, transaction.getMontant());
            stmt.setString(4, transaction.getDevise());
            stmt.setInt(5, transaction.getPointConvertis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}