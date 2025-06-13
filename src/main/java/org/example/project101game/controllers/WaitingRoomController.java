package org.example.project101game.controllers;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.project101game.GameClient;
import org.example.project101game.GameServer;
import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import org.example.project101game.models.ServerCard;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class WaitingRoomController {
    @FXML private Label roomLabel;
    @FXML private Circle avatarCircle;
    @FXML private TextField nameField;
    @FXML private Button randomButton;
    @FXML private Button readyButton;
    @FXML private GridPane avatarGrid;
    @FXML private Label readyLabel;
    @FXML private StackPane rootPane;
    @FXML private Button backToMenuButton;


    private GameClient gameClient; // для клиентов
    private GameServer gameServer; // только для хоста
    private String myClientId;



    boolean isHost = false;
    private boolean isReady = false; // по умолчанию — не готов


    private static String roomCode = "0000";

    private List<Image> avatarImages = new ArrayList<>();
    private List<String> randomNames = Arrays.asList(
            "Игрок1", "Геймер", "Победитель", "Новичок",
            "Эксперт", "Чемпион", "Мастер", "Легенда",
            "Профи", "Стрелок", "Боец", "Счастливчик"
    );

    public static void setRoomCode(String code) {
        roomCode = code;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public void setGameClient(GameClient client) {
        this.gameClient = client;
        this.gameClient.setWaitingRoomController(this);
    }

    public void setMyClientId(String clientId) {
        this.myClientId = clientId;
    }

    @FXML
    public void initialize() {
        // Установка номера комнаты
        // roomLabel.setText("комната #" + roomCode);

        // Загрузка изображений аватаров
        loadAvatarImages();

        // Установка аватара по умолчанию (первого в списке)
        if (!avatarImages.isEmpty()) {
            setAvatar(avatarImages.get(0));
        }

        // Обработчик для кнопки рандомного имени
        randomButton.setOnAction(event -> {
            String randomName = getRandomName();
            nameField.setText(randomName);
        });

        // Добавление обработчиков для кружков с аватарами
        setupAvatarSelection();
    }


    @FXML
    protected void onRenameClick() {
        // логика смены имени игрока
    }

    @FXML
    protected void onRulesClick() {
        SceneSwitcher.switchTo("rules.fxml");
    }

    @FXML
    protected void onStartClick() {
        SceneSwitcher.switchTo("game.fxml");
    }

    @FXML
    private void onReadyClick() {
        isReady = !isReady;
        if (isReady) {
            readyButton.setText("Готов");
            readyButton.setStyle("-fx-background-color: rgba(80, 200, 120, 1); -fx-font-size: 30px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-pref-width: 360; -fx-pref-height: 70;");
            if (isHost) {
                if (gameServer != null) {
                    gameServer.setHostReady(true);
                }
            } else {
                gameClient.sendReady();
            }
        } else {
            readyButton.setText("Не готов");
            readyButton.setStyle("-fx-background-color: rgba(160, 198, 56, 1); -fx-font-size: 30px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-pref-width: 360; -fx-pref-height: 70;");
            if (isHost) {
                if (gameServer != null) {
                    gameServer.setHostReady(false);
                }
            } else {
                gameClient.sendNotReady();
            }
        }
    }



    private void loadAvatarImages() {
        // Загрузка изображений аватаров (замените на ваши пути)
        for (int i = 1; i <= 9; i++) {
            try {
                Image image = new Image(getClass().getResourceAsStream("/org/example/project101game/avatars/avatar" + i + ".jpg"));
                avatarImages.add(image);
            } catch (Exception e) {
                System.err.println("Не удалось загрузить изображение аватара " + i);
                // Добавляем placeholder, если изображение не загружено
                avatarImages.add(null);
            }
        }
    }

    public void setClientAndServer(GameClient client, GameServer server, boolean isHost) {
        this.setGameClient(client);
        this.gameServer = server;
        this.isHost = isHost;
    }


    private void setupAvatarSelection() {
        // Добавляем обработчики для всех кружков в GridPane
        for (int i = 0; i < avatarGrid.getChildren().size(); i++) {
            if (avatarGrid.getChildren().get(i) instanceof Circle circle) {
                int avatarIndex = i;

                // Устанавливаем изображение для кружка, если оно есть
                if (avatarIndex < avatarImages.size() && avatarImages.get(avatarIndex) != null) {
                    circle.setFill(new ImagePattern(avatarImages.get(avatarIndex)));
                }

                // Обработчик выбора аватара
                circle.setOnMouseClicked(event -> {
                    if (avatarIndex < avatarImages.size() && avatarImages.get(avatarIndex) != null) {
                        setAvatar(avatarImages.get(avatarIndex));
                    }
                });
            }
        }
    }

    private void setAvatar(Image image) {
        avatarCircle.setFill(new ImagePattern(image));
    }

    private String getRandomName() {
        Random random = new Random();
        return randomNames.get(random.nextInt(randomNames.size()));
    }

    public void onBackToMenuClick(){
        if(gameServer!=null) gameServer.interrupt();
        try {
            gameClient.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SceneSwitcher.switchTo("/org/example/project101game/menu.fxml");
    }

    // В WaitingRoomController:
    public void onStartGameReceived(List<ServerCard> hand, String currentTurnId, GameClient client) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/project101game/game.fxml"));
                Parent root = loader.load();
                GameController gc = loader.getController();
                gc.initGame(hand, myClientId, currentTurnId, client);
                client.setGameController(gc);
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }



}
