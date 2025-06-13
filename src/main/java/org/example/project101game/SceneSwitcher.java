package org.example.project101game;

import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {
    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void switchTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlFile));
            Rectangle2D d = Screen.getPrimary().getBounds();
            stage.setScene(new Scene(root, d.getWidth(), d.getHeight()));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
