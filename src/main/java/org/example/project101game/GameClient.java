package org.example.project101game;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

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

    public void sendReady() {
        try {
            out.writeUTF("PLAYER_READY");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenToServer() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if ("START_GAME".equals(message)) {
                        System.out.println("Сервер запустил игру");
                        // Здесь вызови метод для смены сцены / переключения FXML
                        onStartGameReceived();

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