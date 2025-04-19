package org.example.project101game.controllers;

import javafx.fxml.FXML;
import org.example.project101game.SceneSwitcher;

public class RulesController {

    @FXML
    protected void onBackClick() {
        SceneSwitcher.switchTo("settings.fxml");
    }
}
