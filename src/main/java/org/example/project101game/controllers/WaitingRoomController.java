package org.example.project101game.controllers;

import org.example.project101game.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class WaitingRoomController {
    @FXML private Label roomLabel;
    @FXML private TextField nameField;
    @FXML private Circle avatarCircle;
    @FXML private GridPane avatarGrid;
    @FXML private Label readyLabel;

    private static String roomCode = "0000";

    public static void setRoomCode(String code) {
        roomCode = code;
    }

    @FXML
    public void initialize() {
        roomLabel.setText("Комната #" + roomCode);
        nameField.setText("Игрок");
        fillAvatarGrid();
    }

    private void fillAvatarGrid() {
        avatarGrid.getChildren().clear();
        for (int i = 0; i < 9; i++) {
            Circle avatar = new Circle(25, Color.LIGHTBLUE);
            avatarGrid.add(avatar, i % 3, i / 3);
        }
    }

    @FXML
    private void handleRandom() {
        String[] names = {"Лис", "Котик", "Панда", "Ёжик", "Сова"};
        String name = names[(int) (Math.random() * names.length)];
        nameField.setText(name);
    }

    @FXML
    private void handleContinue() {
        readyLabel.setText("1 игрок готов"); // Заглушка
    }
//    @FXML private Label roomLabel;
//    @FXML private TextField nameField;
//    @FXML private Button randomButton;
//    @FXML private Button continueButton;
//    @FXML private Circle avatarCircle;
//    @FXML private Label readyLabel;
//
//    @FXML
//    public void initialize() {
//        roomLabel.setText("комната #1234");
//        readyLabel.setText("0 / 4");
//    }
//
//    @FXML
//    private void handleRandom() {
//        nameField.setText("Игрок" + (int)(Math.random()*1000));
//    }
//
//    @FXML
//    private void handleContinue() {
//        System.out.println("Игрок готов: " + nameField.getText());
//        readyLabel.setText("1 / 4"); // Просто пример
//    }
//
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
}
