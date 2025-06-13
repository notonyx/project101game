package org.example.project101game;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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
