package com.example.projetjavafx.root.organizer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdsDashboard  extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ads Overview");

        // Top Bar
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        ComboBox<String> sourceFilter = new ComboBox<>();
        sourceFilter.setPromptText("Source");
        ComboBox<String> campaignTypeFilter = new ComboBox<>();
        campaignTypeFilter.setPromptText("Campaign type");
        ComboBox<String> campaignNameFilter = new ComboBox<>();
        campaignNameFilter.setPromptText("Campaign name");
        topBar.getChildren().addAll(sourceFilter, campaignTypeFilter, campaignNameFilter);

        // KPI Labels
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(20);
        kpiGrid.setPadding(new Insets(10));
        String[] kpiTitles = {"Amount Spent", "Impressions", "Clicks", "CTR", "CPM", "CPC"};
        String[] kpiValues = {"$12,567", "294,480", "10,637", "3.61%", "$42.68", "$1.18"};
        for (int i = 0; i < kpiTitles.length; i++) {
            VBox kpiBox = new VBox(new Label(kpiTitles[i]), new Label(kpiValues[i]));
            kpiGrid.add(kpiBox, i, 0);
        }

        // Line Charts
        LineChart<String, Number> spendChart = createLineChart("Spend amount by Date");
        LineChart<String, Number> impressionsChart = createLineChart("Impressions and Clicks by Date");

        HBox chartRow1 = new HBox(10, spendChart, impressionsChart);

        // Bar Charts
        BarChart<String, Number> impressionsCtrChart = createBarChart("Impressions and CTR over time");
        BarChart<String, Number> clicksChart = createBarChart("Clicks performance over time");

        HBox chartRow2 = new HBox(10, impressionsCtrChart, clicksChart);

        // Pie Chart for Sources
        PieChart sourcePieChart = new PieChart();
        sourcePieChart.getData().addAll(new PieChart.Data("Google Ads", 48.6),
                new PieChart.Data("Facebook Ads", 32.2),
                new PieChart.Data("Others", 19.2));
        sourcePieChart.setTitle("Clicks by Source");

        // Table
        TableView<String> tableView = new TableView<>();
        tableView.setPrefHeight(200);

        VBox bottomSection = new VBox(10, sourcePieChart, tableView);

        // Layout
        VBox mainLayout = new VBox(10, topBar, kpiGrid, chartRow1, chartRow2, bottomSection);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private LineChart<String, Number> createLineChart(String title) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        return lineChart;
    }

    private BarChart<String, Number> createBarChart(String title) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(title);
        return barChart;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
