package org.example.project101game;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private List<ClientHandler> clients = new ArrayList<>();

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
                System.out.println("Новый клиент подключен с IP: " +
                        clientSocket.getInetAddress() + ", порт: " + clientSocket.getPort());
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    // Метод для установки готовности хоста (хост — первый клиент в списке, например)
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
        System.out.println("Игра начинается!");
        for (ClientHandler client : clients) {
            try {
                client.out.writeUTF("START_GAME");
                client.out.flush();
                System.out.println("Отправлено START_GAME клиенту: " + client.socket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private GameServer server;
        private DataInputStream in;
        private DataOutputStream out;
        boolean isReady = false;

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
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
