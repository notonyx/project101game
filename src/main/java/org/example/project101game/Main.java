package org.example.project101game;

import javafx.application.Application;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.project101game.controllers.GameController;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        SceneSwitcher.setStage(stage);
        SceneSwitcher.switchTo("menu.fxml");

    }

    public static void main(String[] args) {
        launch();
    }
}
