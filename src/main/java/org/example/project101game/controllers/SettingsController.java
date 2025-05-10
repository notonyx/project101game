package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.project101game.SceneSwitcher;

import java.io.IOException;
import java.util.EventObject;

public class SettingsController {

    @FXML private CheckBox musicCheckBox;
    @FXML private Slider volumeSlider;
    @FXML private ChoiceBox<String> themeChoice;

    @FXML
    protected void onRulesClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project101game/rules.fxml"));
            Parent root = loader.load();

            Stage rulesStage = new Stage();
            rulesStage.initModality(Modality.APPLICATION_MODAL);
            rulesStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            rulesStage.setTitle("Правила игры");
            rulesStage.setScene(new Scene(root));
            rulesStage.setResizable(false);
            rulesStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        // Закрытие текущего окна
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onMusicSwap(ActionEvent actionEvent) {
        System.out.println("список музыки");
    }

    @FXML
    private void onDevelopersClick(ActionEvent event) {
        SceneSwitcher.switchTo("menu.fxml");
    }

    @FXML
    public void initialize() {
        themeChoice.getItems().addAll("Классика", "Тёмная", "Природа");
        themeChoice.setValue("Классика");
    }
}
