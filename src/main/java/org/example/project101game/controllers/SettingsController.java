package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import org.example.project101game.SceneSwitcher;

public class SettingsController {

    @FXML
    private ToggleButton musicToggle;

    @FXML
    protected void onRulesClick() {
        SceneSwitcher.switchTo("rules.fxml");
    }

    @FXML
    protected void onBackClick() {
        SceneSwitcher.switchTo("menu.fxml");
    }

    @FXML
    public void onMusicSwap(ActionEvent actionEvent) {
        System.out.println("список музыки");
    }
}
