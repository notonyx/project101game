package org.example.project101game.controllers;

import javafx.fxml.FXML;
import org.example.project101game.SceneSwitcher;

public class GameController {

    @FXML
    protected void onBackClick() {
        SceneSwitcher.switchTo("menu.fxml");
    }

    @FXML
    protected void onSettingsClick() {
        SceneSwitcher.switchTo("settings.fxml");
    }
}
