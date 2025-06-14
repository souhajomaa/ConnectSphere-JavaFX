package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.group.GroupDetailsRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.util.Base64;

public class GroupDetailsController {
    @FXML
    private ImageView groupImage;

    @FXML
    private Label groupName;

    @FXML
    private Text groupDescription;

    @FXML
    private Text groupRules;



    @FXML
    public void initialize() {
        // Appliquer le style CSS global à la fenêtre popup
        groupImage.getStyleClass().add("popup-image");
        groupName.getStyleClass().add("popup-title");
        groupDescription.getStyleClass().add("popup-text");
        groupRules.getStyleClass().add("popup-text");
    }


    public void setGroupDetails(String name, String description, String rules, String imageBase64) {
        groupName.setText(name);
        groupDescription.setText(description);
        groupRules.setText("Rules: " + rules);

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            groupImage.setImage(image);
        }
    }

    @FXML
    private void closeWindow() {
        // Fermer la fenêtre actuelle
        Stage stage = (Stage) groupImage.getScene().getWindow();
        stage.close();
    }
}

