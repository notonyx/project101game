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

    public static GameClient client;

    public static Stage getStage(){
        return stage;
    }
    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
        stage.setOnCloseRequest((e) -> {
            if (client != null)
                client.sendDisconnect();
            System.exit(0);
        });
        stage.setMaximized(true);
    }

    public static void switchTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlFile));
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
