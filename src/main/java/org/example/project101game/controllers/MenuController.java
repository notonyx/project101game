package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import org.example.project101game.GameClient;
import org.example.project101game.GameServer;
import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import javafx.scene.control.TextField;



public class MenuController {

    @FXML
    private TextField ipPortField; // Поле для IP хоста


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
    private void onCreateRoomClick() {
        try {

            String[] lst = ipPortField.getText().split(":");
            String hostIP = lst[0];
            int port = Integer.parseInt(lst[1]);
            GameServer server = new GameServer(port);
            server.start(); // Запуск сервера
            GameClient client = new GameClient();
            client.connect(hostIP, port); // ip в хамачи мой (хоста)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project101game/waiting-room.fxml"));
            Parent root = loader.load();


            WaitingRoomController controller = loader.getController();
            controller.setGameClient(client);
            controller.setHost(true); // передаем, что это хост
            controller.setClientAndServer(null, server, true);  // клиент = null, сервер = server, isHost = true

            Stage stage = (Stage) ipPortField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onJoinClick() {
        try {
            String[] lst = ipPortField.getText().split(":");
            String hostIP = lst[0];
            int port = Integer.parseInt(lst[1]);

            GameClient client = new GameClient();
            if (client.connect(hostIP, port)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project101game/waiting-room.fxml"));
                Parent root = loader.load();

                WaitingRoomController controller = loader.getController();
                controller.setGameClient(client);
                controller.setHost(false); // передаем, что это не хост
                controller.setClientAndServer(client, null, false);  // клиент = client, сервер = null, isHost = false
                Stage stage = (Stage) ipPortField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

                System.out.println("ПОЛЬЗОВАТЕЛЬ ПОДКЛЮЧИЛСЯ");
            } else {
                showAlert("Ошибка", "Не удалось подключиться!");
            }
        } catch (IOException e) {
            e.printStackTrace();
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