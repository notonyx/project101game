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

    // Обработчик клиентов
    private static class ClientHandler extends Thread {
        private Socket socket;
        private GameServer server;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // Пример: получение кода комнаты от клиента
                String clientCode = in.readUTF();
                out.writeBoolean(true); // Подтверждение (можно добавить проверку)

                // Здесь можно добавить логику синхронизации игроков

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