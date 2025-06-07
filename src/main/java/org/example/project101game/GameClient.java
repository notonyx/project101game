package org.example.project101game;


import org.example.project101game.controllers.WaitingRoomController;
import org.example.project101game.models.Rank;
import org.example.project101game.models.ServerCard;
import org.example.project101game.models.Suit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private WaitingRoomController waitingRoomController;

    private List<ServerCard> initialHand;
    private String currentTurnId;
    private String myClientId; // инициализируется в menucontroller

    public String getMyClientId() {
        return myClientId;
    }


    public void setWaitingRoomController(WaitingRoomController controller) {
        this.waitingRoomController = controller;
    }


    public boolean connect(String hostIP, int port) {
        try {
            socket = new Socket(hostIP, port);
            myClientId = socket.getLocalAddress().getHostAddress();
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

    private void listenToServer() {
        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();

                    if (msg.startsWith("hand:")) {
                        // Формат: hand:SIX-HEARTS,QUEEN-SPADES,...
                        initialHand = new ArrayList<>();
                        String payload = msg.substring(5);
                        for (String token : payload.split(",")) {
                            String[] parts = token.split("-");
                            Suit suit = Suit.valueOf(parts[1]);
                            Rank rank = Rank.valueOf(parts[0]);
                            initialHand.add(new org.example.project101game.models.ServerCard(suit, rank));
                        }
                    } else if (msg.startsWith("turn:")) {
                        // Формат: turn:PLAYER_ID
                        currentTurnId = msg.substring(5);
                    } else if ("START_GAME".equals(msg)) {
                        // Всё готово — переключаем сцену и передаём данные
                        if (waitingRoomController != null) {
                            waitingRoomController.onStartGameReceived(initialHand, currentTurnId);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Связь с сервером прервана");
            }
        }).start();
    }
}