package org.example.project101game;


import org.example.project101game.controllers.WaitingRoomController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private WaitingRoomController waitingRoomController;

    public void setWaitingRoomController(WaitingRoomController controller) {
        this.waitingRoomController = controller;
    }


    public boolean connect(String hostIP, int port) {
        try {
            socket = new Socket(hostIP, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            listenToServer();  // Запускаем слушатель сообщений от сервера


            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNotReady() {
        try {
            out.writeUTF("PLAYER_NOT_READY");
            out.flush();
            System.out.println("Я НЕ ГОТОВ - отправил серверу");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendReady() {
        try {
            out.writeUTF("PLAYER_READY");
            out.flush();
            System.out.println("Я ГОТОВ - отправил серверу");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenToServer() {
        System.out.println("Клиент начал слушать сервер"); // ← ЭТО
        new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    System.out.println("Получено сообщение от сервера: " + message);
                    if ("START_GAME".equals(message)) {
                        System.out.println("Сервер запустил игру");
                        // Здесь вызови метод для смены сцены / переключения FXML
                        if (waitingRoomController != null) {
                            waitingRoomController.onStartGameReceived();
                        }

                    }
                }
            } catch (IOException e) {
                System.out.println("Связь с сервером прервана");
            }
        }).start();
    }



    private void onStartGameReceived() {
        // TODO: Здесь переключение на нужный FXML (делай через Platform.runLater в контроллере)
        System.out.println("Игра начинается! Здесь надо менять экран.");

    }


}