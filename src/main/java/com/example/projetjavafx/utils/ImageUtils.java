package com.example.projetjavafx.utils;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.util.Base64;

/**
 * Utility class for handling image operations including Base64 conversion
 */
public class ImageUtils {
    
    /**
     * Converts an image file to Base64 string
     * @param imageFile The image file to convert
     * @return Base64 encoded string of the image
     * @throws IOException if file reading fails
     */
    public static String imageToBase64(File imageFile) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(imageFile);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }
    
    /**
     * Converts a Base64 string to JavaFX Image
     * @param base64String The Base64 encoded image string
     * @return JavaFX Image object
     * @throws IllegalArgumentException if Base64 string is invalid
     */
    public static Image base64ToImage(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            return new Image(new ByteArrayInputStream(imageBytes));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 string: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Opens a file chooser dialog for selecting image files
     * @param ownerWindow The parent window for the dialog
     * @param title The title of the file chooser dialog
     * @return Selected file or null if cancelled
     */
    public static File chooseImageFile(Window ownerWindow, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title != null ? title : "Choose an image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        return fileChooser.showOpenDialog(ownerWindow);
    }
    
    /**
     * Loads a default profile image from resources
     * @return Default profile image
     */
    public static Image getDefaultProfileImage() {
        try {
            InputStream is = ImageUtils.class.getResourceAsStream("/images/default-avatar.png");
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Validates if a string is a valid Base64 encoded image
     * @param base64String The string to validate
     * @return true if valid Base64 image, false otherwise
     */
    public static boolean isValidBase64Image(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return false;
        }
        
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            // Try to create an image to validate the data
            Image testImage = new Image(new ByteArrayInputStream(imageBytes));
            return !testImage.isError();
        } catch (Exception e) {
            return false;
        }
    }
}