package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.profile.ProfileRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class UserSearchDropdownController {
    @FXML
    private VBox dropdownContainer;
    
    @FXML
    private VBox resultsContainer;
    
    @FXML
    private Label noResultsLabel;
    
    /**
     * Updates the dropdown with search results
     * @param users List of user profiles to display
     */
    public void updateResults(List<ProfileRepository> users) {
        // Clear previous results
        resultsContainer.getChildren().clear();
        
        if (users == null || users.isEmpty()) {
            // Show no results message
            noResultsLabel.setVisible(true);
            noResultsLabel.setManaged(true);
            // Show the dropdown even when empty
            dropdownContainer.setVisible(true);
            dropdownContainer.setManaged(true);
            // Ensure dropdown is on top
            ensureDropdownVisibility();
        } else {
            // Hide no results message
            noResultsLabel.setVisible(false);
            noResultsLabel.setManaged(false);
            
            // Add each user to the results container
            for (ProfileRepository user : users) {
                addUserToResults(user);
            }
            
            // Show the dropdown
            dropdownContainer.setVisible(true);
            dropdownContainer.setManaged(true);
            // Ensure dropdown is on top
            ensureDropdownVisibility();
        }
    }
    
    /**
     * Ensures the dropdown is visible and on top of other elements
     */
    private void ensureDropdownVisibility() {
        // Bring to front
        dropdownContainer.toFront();
        
        // Reset style first to avoid duplication
        String baseStyle = dropdownContainer.getStyle().replaceAll("-fx-z-index:[^;]+;?", "")
                .replaceAll("-fx-view-order:[^;]+;?", "");
        
        // Set extremely high z-index and extremely negative view order for better stacking
        dropdownContainer.setStyle(baseStyle + 
                "; -fx-z-index: 999999; -fx-view-order: -999999;");
        
        // Use Platform.runLater to ensure UI updates happen on JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            // Force parent to update layout
            if (dropdownContainer.getParent() != null) {
                // Get the scene
                if (dropdownContainer.getScene() != null) {
                    // Find the root of the scene
                    javafx.scene.Parent root = dropdownContainer.getScene().getRoot();
                    
                    // If the dropdown is not already in the scene graph, add it to the root
                    if (root instanceof javafx.scene.layout.Pane) {
                        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) root;
                        
                        // Check if the dropdown is already a child of the root
                        if (!rootPane.getChildren().contains(dropdownContainer)) {
                            // Get the position of the search field in scene coordinates
                            javafx.scene.Node searchField = dropdownContainer.getScene().lookup("#userSearchField");
                            if (searchField != null) {
                                // Calculate position relative to the root
                                javafx.geometry.Bounds bounds = searchField.localToScene(searchField.getBoundsInLocal());
                                
                                // Position the dropdown below the search field
                                dropdownContainer.setLayoutX(bounds.getMinX());
                                dropdownContainer.setLayoutY(bounds.getMaxY());
                                
                                // Add the dropdown to the root pane
                                rootPane.getChildren().add(dropdownContainer);
                            }
                        }
                    }
                }
                
                // Ensure the dropdown is the last child in its parent's children list
                // which helps with z-ordering in JavaFX
                javafx.scene.Parent parent = dropdownContainer.getParent();
                if (parent instanceof javafx.scene.layout.Pane) {
                    javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
                    if (pane.getChildren().contains(dropdownContainer)) {
                        // Remove and add back to make it the last child (top-most in z-order)
                        pane.getChildren().remove(dropdownContainer);
                        pane.getChildren().add(dropdownContainer);
                    }
                }
                
                // Request layout update
                parent.requestLayout();
            }
            
            // Request layout to ensure changes take effect
            dropdownContainer.requestLayout();
            
            // Force to front again after layout
            dropdownContainer.toFront();
        });
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
     * Hides the dropdown
     */
    public void hideDropdown() {
        dropdownContainer.setVisible(false);
        dropdownContainer.setManaged(false);
    }
    
    /**
     * Checks if the dropdown is currently visible
     * @return true if visible, false otherwise
     */
    public boolean isVisible() {
        return dropdownContainer.isVisible();
    }
}