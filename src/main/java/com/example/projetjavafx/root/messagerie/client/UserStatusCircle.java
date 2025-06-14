package com.example.projetjavafx.root.messagerie.client;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class UserStatusCircle {

    public static Circle createStatusCircle(double radius) {
        Circle circle = new Circle(radius);
        // Default color for all users
        circle.setFill(Color.GRAY);
        return circle;
    }
}


