package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class MenuController {

    @FXML
    private TextField roomCodeField;

    @FXML
    protected void onJoinClick() {
        SceneSwitcher.switchTo("waiting-room.fxml");
    }

    @FXML
    protected void onSettingsClick() {
        SceneSwitcher.switchTo("settings.fxml");
    }

    @FXML
    public void joinRoom(ActionEvent actionEvent) {
        System.out.println("Присоединились!");
        // здесь переход в комнату
    }
}
