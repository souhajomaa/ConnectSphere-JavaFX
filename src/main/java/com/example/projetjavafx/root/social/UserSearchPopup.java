package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.profile.ProfileRepository;
import com.example.projetjavafx.root.group.GroupProfileRepository;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * A popup component for displaying user and group search results
 * This class uses JavaFX's Popup which is designed to float above all other UI elements
 */
public class UserSearchPopup extends Popup {
    
    private VBox contentContainer;
    private VBox resultsContainer;
    private Label noResultsLabel;
    
    /**
     * Constructor - initializes the popup with a styled container
     */
    public UserSearchPopup() {
        // Make sure popup doesn't steal focus
        setAutoHide(true);
        setHideOnEscape(true);
        setAutoFix(true);
        
        // Create the main container
        contentContainer = new VBox();
        contentContainer.setMaxHeight(300);
        contentContainer.setPrefWidth(300);
        contentContainer.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 0 0 8 8; " +
                "-fx-border-radius: 0 0 8 8; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 0 1 1 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 3);");
        
        // Create scroll pane for results
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        scrollPane.setMaxHeight(300);
        
        // Create results container
        resultsContainer = new VBox();
        resultsContainer.setSpacing(2);
        scrollPane.setContent(resultsContainer);
        
        // Create no results label
        noResultsLabel = new Label("No results found");
        noResultsLabel.setAlignment(javafx.geometry.Pos.CENTER);
        noResultsLabel.setManaged(false);
        noResultsLabel.setVisible(false);
        noResultsLabel.setStyle("-fx-padding: 15; -fx-font-size: 13px; -fx-text-fill: #666;");
        
        // Add components to container
        contentContainer.getChildren().addAll(scrollPane, noResultsLabel);
        
        // Add container to popup
        getContent().add(contentContainer);
    }
    
    /**
     * Updates the popup with user search results
     * @param users List of user profiles to display
     */
    public void updateResults(List<ProfileRepository> users) {
        // Clear previous results
        resultsContainer.getChildren().clear();
        
        if (users == null || users.isEmpty()) {
            // Show no results message
            noResultsLabel.setVisible(true);
            noResultsLabel.setManaged(true);
        } else {
            // Hide no results message
            noResultsLabel.setVisible(false);
            noResultsLabel.setManaged(false);
            
            // Add each user to the results container
            for (ProfileRepository user : users) {
                addUserToResults(user);
            }
        }
    }
    
    /**
     * Updates the popup with group search results
     * @param groups List of group profiles to display
     */
    public void updateGroupResults(List<GroupProfileRepository> groups) {
        // Clear previous results
        resultsContainer.getChildren().clear();
        
        if (groups == null || groups.isEmpty()) {
            // Show no results message
            noResultsLabel.setVisible(true);
            noResultsLabel.setManaged(true);
        } else {
            // Hide no results message
            noResultsLabel.setVisible(false);
            noResultsLabel.setManaged(false);
            
            // Add each group to the results container
            for (GroupProfileRepository group : groups) {
                addGroupToResults(group);
            }
        }
    }
    
    /**
     * Updates the popup with both user and group search results
     * @param users List of user profiles to display
     * @param groups List of group profiles to display
     */
    public void updateCombinedResults(List<ProfileRepository> users, List<GroupProfileRepository> groups) {
        // Clear previous results
        resultsContainer.getChildren().clear();
        
        boolean hasResults = false;
        
        // Add users if any
        if (users != null && !users.isEmpty()) {
            // Add a label for users section
            Label usersLabel = new Label("Users");
            usersLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 10 2 10; -fx-text-fill: #555;");
            resultsContainer.getChildren().add(usersLabel);
            
            // Add each user
            for (ProfileRepository user : users) {
                addUserToResults(user);
            }
            hasResults = true;
        }
        
        // Add groups if any
        if (groups != null && !groups.isEmpty()) {
            // Add a label for groups section
            Label groupsLabel = new Label("Groups");
            groupsLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 10 2 10; -fx-text-fill: #555;");
            resultsContainer.getChildren().add(groupsLabel);
            
            // Add each group
            for (GroupProfileRepository group : groups) {
                addGroupToResults(group);
            }
            hasResults = true;
        }
        
        // Show no results message if neither users nor groups were found
        if (!hasResults) {
            noResultsLabel.setVisible(true);
            noResultsLabel.setManaged(true);
        } else {
            noResultsLabel.setVisible(false);
            noResultsLabel.setManaged(false);
        }
    }
    
    /**
     * Shows the popup below the specified node
     * @param anchor The node to position the popup below
     */
    public void showBelow(Node anchor) {
        if (anchor == null) return;
        
        // Get the position of the anchor in screen coordinates
        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        
        // Position the popup below the anchor
        show(anchor.getScene().getWindow(), 
             bounds.getMinX(), 
             bounds.getMaxY());
    }
    
    /**
     * Adds a single user to the results container
     * @param profile The user profile to add
     */
    private void addUserToResults(ProfileRepository profile) {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/projetjavafx/social/UserSearchResult.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: UserSearchResult.fxml not found");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Node userNode = loader.load();
            resultsContainer.getChildren().add(userNode);
            
            UserSearchResultController controller = loader.getController();
            controller.setData(profile);
        } catch (IOException e) {
            System.err.println("Error loading UserSearchResult.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a single group to the results container
     * @param group The group profile to add
     */
    private void addGroupToResults(GroupProfileRepository group) {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/projetjavafx/social/GroupSearchResult.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: GroupSearchResult.fxml not found");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Node groupNode = loader.load();
            resultsContainer.getChildren().add(groupNode);
            
            GroupSearchResultController controller = loader.getController();
            controller.setData(group);
        } catch (IOException e) {
            System.err.println("Error loading GroupSearchResult.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean contains(double x, double y) {
        // Get the bounds of the content container in screen coordinates
        Bounds bounds = contentContainer.localToScreen(contentContainer.getBoundsInLocal());
        return bounds.contains(x, y);
    }
}