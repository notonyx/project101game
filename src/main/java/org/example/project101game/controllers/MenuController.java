package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;


public class MenuController {

    @FXML
    private TextField roomCodeField;

    @FXML
    protected void onJoinClick() {
        SceneSwitcher.switchTo("waiting-room.fxml");
    }

    @FXML
    private void onSettingsClick(ActionEvent event) {
        try {
            // Загрузка FXML настроек
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project101game/settings.fxml"));
            Parent root = loader.load();

            // Создание нового окна
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Настройки");
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Установка размеров
            settingsStage.setScene(new Scene(root, 500, 600));
            settingsStage.setResizable(false);
            settingsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onCreateRoomClick() {
        SceneSwitcher.switchTo("settings.fxml");
    }

    @FXML
    public void joinRoom(ActionEvent actionEvent) {
        System.out.println("Присоединились!");
        // здесь переход в комнату
    }

}
