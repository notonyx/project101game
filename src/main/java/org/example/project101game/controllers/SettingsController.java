package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.project101game.SceneSwitcher;

public class SettingsController {

    @FXML private CheckBox musicCheckBox;
    @FXML private Slider volumeSlider;
    @FXML private ChoiceBox<String> themeChoice;

    @FXML
    protected void onRulesClick() {
        SceneSwitcher.switchTo("rules.fxml");
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
