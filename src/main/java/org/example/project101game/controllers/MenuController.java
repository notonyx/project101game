package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import org.example.project101game.GameClient;
import org.example.project101game.GameServer;
import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;



public class MenuController {

    @FXML private TextField ipPortField; // Поле для IP хоста


    @FXML
    protected void onSettingsClick() {
        SceneSwitcher.switchTo("settings.fxml");
    }

    @FXML
    private void onCreateRoomClick() {
        int port = Integer.parseInt(ipPortField.getText());
        new GameServer(port).start(); // Запуск сервера
        SceneSwitcher.switchTo("waiting-room.fxml");
    }

    // Подключение к комнате (клиент)
    @FXML
    private void onJoinClick() {
        String [] lst = ipPortField.getText().split(":");
        String hostIP = lst[0];
        int port = Integer.parseInt(lst[1]);

        GameClient client = new GameClient();
        if (client.connect(hostIP, port)) {
            SceneSwitcher.switchTo("waiting-room.fxml");
            System.out.println("ПОЛЬЗОВАТЕЛЬ ПОДКЛЮЧИЛСЯ");
        } else {
            showAlert("Ошибка", "Не удалось подключиться!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}