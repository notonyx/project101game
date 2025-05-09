package org.example.project101game;
import java.io.*;
import java.net.*;

public class NetworkManager {
    // Порт по умолчанию
    public static final int DEFAULT_PORT = 5555;

    // Проверка подключения к серверу
    public static boolean testConnection(String hostIP, int port) {
        try (Socket socket = new Socket(hostIP, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}