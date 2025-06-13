package org.example.project101game;

import javafx.scene.image.Image;
import org.example.project101game.models.Rank;
import org.example.project101game.models.ServerCard;
import org.example.project101game.models.Suit;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private List<ClientHandler> clients = new ArrayList<>();
    private List<ServerCard> deck;  // приватное поле для колоды
    private Map<String, List<ServerCard>> playerHands = new HashMap<>(); // руки клиентов
    private List<ServerCard> discardPile = new ArrayList<>(); // сброс
    private int currentPlayerIndex = 0; // index игрока чей ход


    private void initializeDeck() {
        deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new ServerCard(suit, rank));
            }
        }
        Collections.shuffle(deck);
    }

    private void dealCards(List<String> playerIds, int cardsCount) {
        for (String playerId : playerIds) {
            List<ServerCard> hand = new ArrayList<>();
            for (int i = 0; i < cardsCount; i++) {
                hand.add(deck.remove(0));
            }
            playerHands.put(playerId, hand);
        }
    }

    public GameServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новый клиент подключен с IP: " + clientSocket.getInetAddress() + ", порт: " + clientSocket.getPort());
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    // Метод для установки готовности хоста (хост — первый клиент в списке)
    public synchronized void setHostReady(boolean ready) {
        if (clients.isEmpty()) return;  // если нет клиентов — ничего не делать
        ClientHandler hostClient = clients.get(0); // считаем, что хост — первый клиент
        hostClient.setReady(ready);
        System.out.println("Хост готов: " + ready);
        checkAllReady();
    }

    synchronized void checkAllReady() {
        boolean allClientsReady = clients.stream().allMatch(c -> c.isReady);
        if (allClientsReady) {
            startGameCountdown();
        }
    }

    void startGameCountdown() {
        System.out.println("Все готовы! Игра начнётся через 3 секунды...");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startGame();
            }
        }, 3000);
    }

    public void startGame() {
        // 1) Подготовка колоды и раздача
        initializeDeck(); // рандомно генерируемая колода
        List<String> playerIds = clients.stream().map(c -> c.socket.getInetAddress().getHostAddress()).toList();
        dealCards(playerIds, 5);

        // 2) Логирование раздачи
        System.out.println("=== Раздача карт ===");
        playerHands.forEach((playerId, hand) -> {
            System.out.print("Игрок " + playerId + " получает: ");
            hand.forEach(card -> System.out.print(card.getRank() + "-" + card.getSuit() + " "));
            System.out.println();
        });

        // 3) Определяем первого ходящего
        currentPlayerIndex = new Random().nextInt(clients.size());
        String firstPlayer = playerIds.get(currentPlayerIndex);
        System.out.println("Первый ход делает: " + firstPlayer);

        // 4) Отправляем сначала руки всем
        for (ClientHandler c : clients) {
            String ip = c.socket.getInetAddress().getHostAddress();
            try {
                sendInitialHandToPlayer(ip, playerHands.get(ip));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 5) Только после этого посылаем START_GAME всем
        for (ClientHandler c : clients) {
            String ip = c.socket.getInetAddress().getHostAddress();
            try {
                String turnMsg = "turn:" + firstPlayer;
                c.out.writeUTF(turnMsg);
                c.out.flush();
                System.out.println("Отправлено " + turnMsg + " клиенту: " + ip);
                c.out.writeUTF("START_GAME");
                c.out.flush();
                System.out.println("Отправлено START_GAME клиенту: " + ip);
                // и сразу уведомляем о ходе

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // уведомить всех, чей сейчас ход
    private void notifyTurnToAll() {
        String currentPlayerId = clients.get(currentPlayerIndex).socket.getInetAddress().getHostAddress();
        String msg = "turn:" + currentPlayerId;
        System.out.println(msg);
        for (ClientHandler c : clients) {
            sendMessageToClient(c.socket.getInetAddress().getHostAddress(), msg);
        }
    }

    // после того, как текущий игрок сделал ход:
    private void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        notifyTurnToAll();
    }

    private void sendInitialHandToPlayer(String playerId, List<ServerCard> hand) throws IOException {
        StringBuilder sb = new StringBuilder("hand:");
        for (int i = 0; i < hand.size(); i++) {
            ServerCard card = hand.get(i);
            sb.append(card.getRank().name()).append("-").append(card.getSuit().name());
            if (i < hand.size() - 1) sb.append(",");
        }
        String msg = sb.toString();
        // логируем
        System.out.println("Отправка игроку " + playerId + " -> " + msg);
        sendMessageToClient(playerId, msg);
    }

    private void sendMessageToClient(String playerId, String message) {
        for (ClientHandler c : clients) {
            String ip = c.socket.getInetAddress().getHostAddress();
            if (ip.equals(playerId)) {
                try {
                    c.out.writeUTF(message);
                    c.out.flush();
                } catch (IOException e) {
                    System.err.println("Ошибка отправки " + message + " клиенту " + ip);
                }
                break;
            }
        }
    }

    private void sentPlayedCard(String message) {
        for (ClientHandler c : clients) {
            String ip = c.socket.getInetAddress().getHostAddress();
            try {
                c.out.writeUTF(message);
                c.out.flush();
                System.out.println("Отправлено " + message + " клиенту: " + ip);
                // и сразу уведомляем о ходе

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleDrawCard(ClientHandler handler) {
        String playerId = handler.socket.getInetAddress().getHostAddress();
        if (deck.isEmpty() && discardPile.size() > 1) {
            // Сохраняем последнюю карту
            ServerCard topCard = discardPile.get(discardPile.size() - 1);

            // Забираем остальные карты и замешиваем
            List<ServerCard> reshuffled = new ArrayList<>(discardPile.subList(0, discardPile.size() - 1));
            Collections.shuffle(reshuffled);

            // Обновляем колоду
            deck.addAll(reshuffled);

            // Оставляем только последнюю карту в сбросе
            discardPile.clear();
            discardPile.add(topCard);

            System.out.println("Колода была пуста. Сброс перемешан обратно в колоду.");
        }

        ServerCard drawnCard = deck.remove(0);
        playerHands.get(playerId).add(drawnCard);

        String msg = "PLAYER_DRAW_CARD:" + drawnCard.getRank().name() + "-" + drawnCard.getSuit().name();
        sendMessageToClient(playerId, msg);
        System.out.println("Игрок " + playerId + " получил карту: " + msg);
        advanceTurn();
    }

    private void addCardToDiscardPile(String message) {
        String cardStr = message.replace("PLAYER_PLAY_CARD:", "").trim();
        String[] parts = cardStr.split(" ");
        Rank rank = Rank.fromSymbol(parts[0]);
        Suit suit = Suit.fromSymbol(parts[1]);
        ServerCard playedCard = new ServerCard(suit, rank);
        discardPile.add(playedCard);
        System.out.println("Карта отправлена в сброс: " + playedCard);
    }


    public ServerCard getTopDiscardCard() { // возможно проблемы с тем как хранится карта, на всякий случай лучше проверить че там
        if (discardPile.isEmpty()) return null;
        return discardPile.get(discardPile.size() - 1);
    }


    private static class ClientHandler extends Thread {
        private Socket socket;
        private GameServer server;
        private DataInputStream in;
        private DataOutputStream out;
        boolean isReady = false;
        private String clientName = "";

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        public void setClientName(String name){
            clientName = name;
        }

        public void setReady(boolean ready) {
            this.isReady = ready;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());


                while (true) {
                    String message = in.readUTF();
                    if ("PLAYER_READY".equals(message)) {
                        setReady(true);
                        System.out.println("Игрок готов: " + socket.getInetAddress());
                        server.checkAllReady();
                    } else if ("PLAYER_NOT_READY".equals(message)) {
                        setReady(false);
                        System.out.println("Игрок НЕ готов: " + socket.getInetAddress());
                        server.checkAllReady();
                    } else if (message.startsWith("PLAYER_PLAY_CARD:")) {
                        System.out.println("Игрок отправил карту " + socket.getInetAddress());
                        server.sentPlayedCard(message);
                        server.advanceTurn();
                        server.addCardToDiscardPile(message);
                    } else if ("PLAYER_DRAW_CARD".equals(message)) {
                        System.out.println("Игрок взял карту " + socket.getInetAddress());
                        server.handleDrawCard(this); // Обработать взятие карты
                    }
                }

            } catch (IOException e) {
                System.out.println("Клиент отключился.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
