package org.example.project101game;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.example.project101game.controllers.WaitingRoomController;
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
    private int clientsCount = 0;
    private int readyCount = 0;

    private Suit currentSuit;
    private Rank currentRank;
    private int skipCounter = 0;
    private int drawCounter = 0;

     private String previousRoundWinner;

    private final WaitingRoomController waitingRoomController;


    @Override
    public void interrupt() {
        clients.forEach(o -> {
            try{
                o.socket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        });
        try{
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }

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
        // Проверяем, что в колоде достаточно карт
        int requiredCards = playerIds.size() * cardsCount;
        if (deck.size() < requiredCards) {
            throw new IllegalStateException("Недостаточно карт в колоде для раздачи");
        }
        for (String playerId : playerIds) {
            List<ServerCard> hand = new ArrayList<>();
            for (int i = 0; i < cardsCount; i++) {
                ServerCard card = deck.remove(0);
                // Дополнительная проверка на уникальность в руке
                if (hand.contains(card)) {
                    throw new IllegalStateException("Повторяющаяся карта в руке игрока");
                }
                hand.add(card);
            }
            playerHands.put(playerId, hand);
        }
    }
    ///////////////////// не использованные приколы для результатов, там надо в геймклайент добавлять много нового
    private Map<String, Integer> calculateScores() {
        Map<String, Integer> scores = new HashMap<>();
        for (Map.Entry<String, List<ServerCard>> entry : playerHands.entrySet()) {
            int score = entry.getValue().stream()
                .mapToInt(c -> c.getRank().getValue())
                .sum();
            scores.put(entry.getKey(), score);
        }
        return scores;
    }

    private String buildResultMessage(String winnerId, Map<String, Integer> scores) {
        StringBuilder sb = new StringBuilder("ROUND_RESULTS:");
        sb.append(winnerId).append(";");
        scores.forEach((playerId, score) -> {
            sb.append(playerId).append("=").append(score).append(",");
        });
        sb.deleteCharAt(sb.length() - 1); // Удаляем последнюю запятую
        return sb.toString();
    }

    private void broadcastResult(String message) {
        for (ClientHandler client : clients) {
            try {
                client.out.writeUTF(message);
                client.out.flush();
            } catch (IOException e) {
                System.err.println("Ошибка отправки результатов клиенту: " + e.getMessage());
            }
        }
    }

    private void logRoundResults(String winnerId, Map<String, Integer> scores) {
        System.out.println("\n=== Результаты раунда ===");
        System.out.println("Победитель: " + winnerId);
        System.out.println("Очки игроков:");
        scores.forEach((playerId, score) ->
            System.out.println(playerId + ": " + score + " очков"));
        System.out.println("=======================\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    private String getCurrentPlayerId() {
        ClientHandler currentPlayer = clients.get(currentPlayerIndex);
        return currentPlayer.socket.getInetAddress().getHostAddress() + ":" + currentPlayer.socket.getPort();
    }

     private void handlePlayCard(ClientHandler handler, String message) {
        String playerId = handler.socket.getInetAddress().getHostAddress()
            .concat(":").concat(String.valueOf(handler.socket.getPort()));

        if (!playerId.equals(getCurrentPlayerId())) {
            System.out.println("Не очередь игрока " + playerId + " ходить!");
            return;
        }

        String cardStr = message.replace("PLAYER_PLAY_CARD:", "").trim();
        String[] parts = cardStr.split(" ");
        Rank rank = Rank.fromSymbol(parts[0]);
        Suit suit = Suit.fromSymbol(parts[1]);
        ServerCard playedCard = new ServerCard(suit, rank);

        // Проверяем валидность хода
        if (!isValidMove(playedCard)) {
            System.out.println("Недопустимый ход игрока " + playerId);
            sendMessageToClient(playerId, "INVALID_MOVE");
            return;
        }

        // Удаляем карту из руки игрока
        List<ServerCard> hand = playerHands.get(playerId);
        hand.removeIf(c -> c.getRank() == rank && c.getSuit() == suit);

        // Добавляем в сброс
        discardPile.add(playedCard);
        currentSuit = playedCard.getSuit();
        currentRank = playedCard.getRank();

        // Обрабатываем специальные карты
        handleSpecialCard(playedCard);

        // Уведомляем всех о сыгранной карте
        broadcastPlayedCard(playedCard);

        // Проверяем конец раунда
        if (hand.isEmpty()) {
            endRound(playerId);
            return;
        }

        // Передаем ход следующему игроку (с учетом пропусков)
        advanceTurn();
    }

    private boolean isValidMove(ServerCard card) {
        if (discardPile.isEmpty()) {
            return true; // Первый ход в раунде
        }

        return card.getSuit() == currentSuit || card.getRank() == currentRank;
    }

    private void handleSpecialCard(ServerCard card) {
        switch (card.getRank()) {
            case SIX:
                drawCounter += 2;
                break;
            case ACE:
                skipCounter += 1;
                break;
            case QUEEN:
                // Масть будет изменена клиентом
                break;
        }
    }

    private void broadcastPlayedCard(ServerCard card) {
        String msg = "PLAYED_CARD:" + card.getRank() + " " + card.getSuit();
        for (ClientHandler c : clients) {
            try {
                c.out.writeUTF(msg);
                c.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void endRound(String winnerId) {
        // Подсчет очков
        Map<String, Integer> scores = new HashMap<>();
        for (Map.Entry<String, List<ServerCard>> entry : playerHands.entrySet()) {
            int score = entry.getValue().stream()
                .mapToInt(c -> c.getRank().getValue())
                .sum();
            scores.put(entry.getKey(), score);
        }

        // Проверка специальных условий (дама в конце)
        ServerCard lastCard = discardPile.get(discardPile.size() - 1);
        if (lastCard.getRank() == Rank.QUEEN) {
            int bonus = (lastCard.getSuit() == Suit.SPADES) ? -40 : -20;
            scores.merge(winnerId, bonus, Integer::sum);
        }

        // Отправка результатов
        StringBuilder resultMsg = new StringBuilder("ROUND_END:");
        resultMsg.append(winnerId).append(";");
        scores.forEach((playerId, score) -> {
            resultMsg.append(playerId).append("=").append(score).append(",");
        });
        resultMsg.deleteCharAt(resultMsg.length() - 1);

        for (ClientHandler c : clients) {
            try {
                c.out.writeUTF(resultMsg.toString());
                c.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Подготовка к новому раунду
        prepareNewRound(winnerId);
    }

    private void prepareNewRound(String winnerId) {
        // Очищаем руки и сброс
        playerHands.clear();
        discardPile.clear();

        // Перемешиваем колоду
        initializeDeck();

        // Раздаем карты
        List<String> playerIds = clients.stream()
            .map(c -> c.socket.getInetAddress().getHostAddress()
                .concat(":").concat(String.valueOf(c.socket.getPort())))
            .toList();
        dealCards(playerIds, 5);

        // Определяем первого ходящего (победитель предыдущего раунда)
        currentPlayerIndex = playerIds.indexOf(winnerId);

        // Отправляем новые руки и уведомляем о начале раунда
        for (ClientHandler c : clients) {
            String id = c.socket.getInetAddress().getHostAddress() + ":" + String.valueOf(c.socket.getPort());
            try {
                sendInitialHandToPlayer(id, playerHands.get(id));
                c.out.writeUTF("NEW_ROUND");
                c.out.flush();
                String turnMsg = "turn:" + winnerId;
                c.out.writeUTF(turnMsg);
                c.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public GameServer(WaitingRoomController controller, int port) {
        this.waitingRoomController = controller;
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
                clientsCount++;
                Platform.runLater(() -> {waitingRoomController.setClientCount(readyCount, clientsCount);});
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
        if(ready)
            incrementReadyCount();
        else
            decrementReadyCount();
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

    public void sendNamesAndAvatars(){
        StringBuilder clInfo = new StringBuilder();
        for (ClientHandler client : clients) {
            clInfo.append(String.format("%s:%s,", client.clientName, String.valueOf(client.pfp)));
        }
        clInfo.deleteCharAt(clInfo.length()-1);
        clients.forEach(c -> {
            try {
                c.out.writeUTF("PLAYERS:" + clients.indexOf(c) + ";" + clInfo.toString());
                c.out.flush();
                System.out.println("Player info sent: " + "PLAYERS:" + clients.indexOf(c) + ";" + clInfo.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void startGame() {
        // 1) Подготовка колоды и раздача

        sendNamesAndAvatars();

        initializeDeck(); // рандомно генерируемая колода
        List<String> playerIds = clients.stream()
                .map(c -> c.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(c.socket.getPort())))
                .toList();
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
            String id = c.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(c.socket.getPort()));
            try {
                sendInitialHandToPlayer(id, playerHands.get(id));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 5) Только после этого посылаем START_GAME всем
        for (ClientHandler c : clients) {
            String id = c.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(c.socket.getPort()));
            try {
                String turnMsg = "turn:" + firstPlayer;
                c.out.writeUTF(turnMsg);
                c.out.flush();
                System.out.println("Отправлено " + turnMsg + " клиенту: " + id);
                c.out.writeUTF("START_GAME");
                c.out.flush();
                System.out.println("Отправлено START_GAME клиенту: " + id);
                // и сразу уведомляем о ходе

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendUpdateClients() {
        for (ClientHandler c : clients) {
            try {
                c.out.writeUTF("count:" + readyCount + "," + clientsCount);
                c.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // уведомить всех, чей сейчас ход
    private void notifyTurnToAll() {
        String currentPlayerId = clients.get(currentPlayerIndex)
                .socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(clients.get(currentPlayerIndex).socket.getPort()));
        String msg = "turn:" + currentPlayerId;
        System.out.println(msg);
        for (ClientHandler c : clients) {
            sendMessageToClient(
                    c.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(c.socket.getPort())),
                    msg
            );
        }
    }

    // после того, как текущий игрок сделал ход:
    private void advanceTurn() {
        // Обработка пропусков хода (для туза)
        if (skipCounter > 0) {
            skipCounter--;
            currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
            notifyTurnToAll();
            return;
        }
        // Обработка взятия карт (для шестерки)
        if (drawCounter > 0) {
            ClientHandler nextPlayer = clients.get((currentPlayerIndex + 1) % clients.size());
            String nextPlayerId = nextPlayer.socket.getInetAddress().getHostAddress() +
                   ":" + nextPlayer.socket.getPort();

            // Даем следующему игроку нужное количество карт
            for (int i = 0; i < drawCounter; i++) {
                if (deck.isEmpty()) {
                    if (discardPile.size() > 1) {
                        ServerCard topCard = discardPile.get(discardPile.size() - 1);
                        List<ServerCard> reshuffled = new ArrayList<>(discardPile.subList(0, discardPile.size() - 1));
                        Collections.shuffle(reshuffled);
                        deck.addAll(reshuffled);
                        discardPile.clear();
                        discardPile.add(topCard);
                        System.out.println("Колода была пуста. Сброс перемешан обратно в колоду.");
                    }
                }
                if (!deck.isEmpty()) {
                    ServerCard drawnCard = deck.remove(0);
                    playerHands.get(nextPlayerId).add(drawnCard);
                    sendMessageToClient(nextPlayerId,
                        "DRAW_CARD:" + drawnCard.getRank().name() + "-" + drawnCard.getSuit().name());
                }
            }

            drawCounter = 0; // Сбрасываем счетчик взятий
            currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        } else {
            // Обычный переход хода
            currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        }

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
            String id = c.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(c.socket.getPort()));
            if (id.equals(playerId)) {
                try {
                    c.out.writeUTF(message);
                    c.out.flush();
                } catch (IOException e) {
                    System.err.println("Ошибка отправки " + message + " клиенту " + id);
                }
                break;
            }
        }
    }

    private void sentPlayedCard(String message) {
        for (ClientHandler c : clients) {
            String id = c.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(c.socket.getPort()));
            try {
                c.out.writeUTF(message);
                c.out.flush();
                System.out.println("Отправлено " + message + " клиенту: " + id);
                // и сразу уведомляем о ходе

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleDrawCard(ClientHandler handler) {
        String playerId = handler.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(clients.get(currentPlayerIndex).socket.getPort()));
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

    public synchronized void incrementReadyCount(){
        readyCount++;
        sendUpdateClients();
    }

    public synchronized void decrementReadyCount() {
        readyCount--;
        sendUpdateClients();
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

    private void lastCard() {

        ServerCard drawnCard = getTopDiscardCard();
        for (ClientHandler c : clients) {
            String playerId = c.socket.getInetAddress().getHostAddress().concat(":").concat(String.valueOf(clients.get(currentPlayerIndex).socket.getPort()));
            String msg = "LAST_CARD:" + drawnCard.getRank().name() + "-" + drawnCard.getSuit().name();
            sendMessageToClient(playerId, msg);
        }


    }

    public synchronized void removeClient(ClientHandler  handler)
    {
        clients.remove(handler);
        clientsCount--;
        if (handler.isReady)
            readyCount--;
        sendUpdateClients();
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
        private byte pfp = 0;

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        public void setClientName(String name){
            clientName = name;
        }

        public void setAvatar(byte id){
            pfp = id;
        }

        public void setReady(boolean ready) {
            this.isReady = ready;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                server.sendUpdateClients();
                while (true) {
                    String message = in.readUTF();
                    if ("PLAYER_READY".equals(message)) {
                        setReady(true);
                        System.out.println("Игрок готов: " + socket.getInetAddress());
                        server.incrementReadyCount();
                        server.checkAllReady();
                    } else if ("PLAYER_NOT_READY".equals(message)) {
                        setReady(false);
                        System.out.println("Игрок НЕ готов: " + socket.getInetAddress());
                        server.decrementReadyCount();
                        server.checkAllReady();
                    } else if (message.startsWith("PLAYER_SET_NAME")) {
                        System.out.println("Игрок сменил имя " + socket.getInetAddress());
                        String[] newName = message.split(":");
                        setClientName(newName[1]);
                    }
                    else if (message.startsWith("PLAYER_SET_AVATAR")) {
                        System.out.println("Игрок сменил аватар " + socket.getInetAddress());
                        setAvatar(Byte.parseByte(message.split(":")[1]));
                    }
                    else if (message.startsWith("PLAYER_PLAY_CARD:")) {
                        System.out.println("Игрок отправил карту " + socket.getInetAddress());
                        String c = message.split(":")[1];
                        if (c.startsWith("A")) {
                            server.advanceTurn();
                            System.out.println("Следующий игрок пропускает ход");
                        }
                        if (c.startsWith("6")) {
                            server.advanceTurn();
                            ClientHandler nextPlayer = server.clients.get(server.currentPlayerIndex);

                            // Даем две карты (без автоматического перехода хода)
                            server.handleDrawCard(nextPlayer);
                            server.handleDrawCard(nextPlayer);
                            System.out.println("Следующий игрок пропускает ход и берет 2 карты");
                        }
                        server.sentPlayedCard(message);
                        server.advanceTurn();
                        server.addCardToDiscardPile(message);
                    } else if (message.startsWith("GET_LAST")) {
                        server.lastCard();
                    } else if ("PLAYER_DRAW_CARD".equals(message)) {
                        System.out.println("Игрок взял карту " + socket.getInetAddress());
                        server.handleDrawCard(this); // Обработать взятие карты
                    } else if ("PLAYER_DISCONNECT".equals(message)) {
                        server.removeClient(this);
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
