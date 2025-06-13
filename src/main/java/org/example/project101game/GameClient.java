package org.example.project101game;


import javafx.application.Platform;
import org.example.project101game.controllers.GameController;
import org.example.project101game.controllers.WaitingRoomController;
import org.example.project101game.models.Card;
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
    public Socket getSocket() {
        return socket;
    }

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private WaitingRoomController waitingRoomController;

    private List<ServerCard> initialHand;
    private String currentTurnId;
    private String myClientId; // инициализируется в menucontroller

    private GameController gameController;

    public String getMyClientId() {
        return myClientId;
    }


    public void setWaitingRoomController(WaitingRoomController controller) {
        this.waitingRoomController = controller;
    }

    public void setGameController(GameController controller) {
        this.gameController = controller;
    }


    public boolean connect(String hostIP, int port) {
        try {
            socket = new Socket(hostIP, port);
            myClientId = socket.getLocalAddress().getHostAddress().concat(":").concat(String.valueOf(socket.getLocalPort()));
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            listenToServer();  // Запускаем слушатель сообщений от сервера


            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendDrawCard() {
        try {
            out.writeUTF("PLAYER_DRAW_CARD");
            out.flush();
            System.out.println("Отправлено: PLAYER_DRAW_CARD");
        } catch (IOException e) {
            e.printStackTrace();
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

    public void sendPlayCard(Card card) {
        try {
            out.writeUTF("PLAYER_PLAY_CARD:" + card.toString());
            out.flush();
            System.out.println("Отправил серверу карту " + card.toString());
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
                            initialHand.add(new ServerCard(suit, rank));
                        }
                    } else if (msg.startsWith("turn:")) {
                        // Формат: turn:PLAYER_ID
                        currentTurnId = msg.substring(5);
                        if (gameController != null) {
                            gameController.setIsMyTurn(currentTurnId.equals(myClientId));
                            gameController.disableDeckAndPlayerHand();

                        } else {
                        }
                    } else if ("START_GAME".equals(msg)) {
                        // Всё готово — переключаем сцену и передаём данные
                        if (waitingRoomController != null) {
                            waitingRoomController.onStartGameReceived(initialHand, currentTurnId, this);
                        }
                    } else if (msg.startsWith("PLAYER_PLAY_CARD:")) {
                        String c = msg.split(":")[1];
                        System.out.println(c);
                         gameController.playedCard(c);
                    } else if (msg.startsWith("PLAYER_DRAW_CARD:")) {

                        String payload = msg.split(":")[1];
                        String[] parts = payload.split("-");
                        Rank rank = Rank.valueOf(parts[0]);
                        Suit suit = Suit.valueOf(parts[1]);
                        ServerCard newCard = new ServerCard(suit, rank);
                        gameController.onCardDrawn(newCard);
                    } else if (msg.startsWith("count:")) {
                        String[] counts = msg.split(":")[1].split(",");
                        Platform.runLater(() -> {waitingRoomController.setClientCount(Integer.parseInt(counts[0]), Integer.parseInt(counts[1]));});
                    }
                }
            } catch (IOException e) {
                System.out.println("Связь с сервером прервана");
            }
        }).start();
    }
}