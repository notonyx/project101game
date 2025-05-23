package org.example.project101game.controllers;

import javafx.scene.control.Button;
import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class WaitingRoomController {
    @FXML private Label roomLabel;
    @FXML private Circle avatarCircle;
    @FXML private TextField nameField;
    @FXML private Button randomButton;
    @FXML private Button continueButton;
    @FXML private GridPane avatarGrid;
    @FXML private Label readyLabel;

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

    @FXML
    public void initialize() {
        // Установка номера комнаты
        roomLabel.setText("комната #" + roomCode);

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

    private void setupAvatarSelection() {
        // Добавляем обработчики для всех кружков в GridPane
        for (int i = 0; i < avatarGrid.getChildren().size(); i++) {
            if (avatarGrid.getChildren().get(i) instanceof Circle) {
                Circle circle = (Circle) avatarGrid.getChildren().get(i);
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
}
