package org.example.project101game.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.project101game.SceneSwitcher;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameController {

    @FXML private HBox playerHand;
    @FXML private ImageView deckView;
    @FXML private ImageView discardPileView;
    @FXML private VBox opponent1Box, opponent2Box, opponent3Box;
    @FXML private Circle opponent1Avatar, opponent2Avatar, opponent3Avatar;
    @FXML private Label opponent1Cards, opponent2Cards, opponent3Cards;

    private List<Image> playerCards = new ArrayList<>();
    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 9;

    @FXML
    protected void onBackClick() {
        SceneSwitcher.switchTo("menu.fxml");
    }

//    @FXML
//    protected void onSettingsClick() {
//        SceneSwitcher.switchTo("settings.fxml");
//    }
    @FXML
    private void onSettingsClick(ActionEvent event) {
        try {
            // Загрузка FXML настроек
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project101game/settings.fxml"));
            Parent root = loader.load();

            // Создание нового окна
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Настройки");
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Установка размеров
            settingsStage.setScene(new Scene(root, 500, 600));
            settingsStage.setResizable(false);
            settingsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     @FXML
    public void initialize() {
        // Загрузка аватаров противников (пример)
        loadAvatars();

        // Инициализация тестовых данных
        initTestData();

        // Отображение первой страницы карт
        showPlayerCardsPage();
    }

     private void loadAvatars() {
        try {
            Image avatar1 = new Image(getClass().getResourceAsStream("/org/example/project101game/avatars/avatar1.png"));
            Image avatar2 = new Image(getClass().getResourceAsStream("/org/example/project101game/avatars/avatar2.png"));
            Image avatar3 = new Image(getClass().getResourceAsStream("/org/example/project101game/avatars/avatar3.png"));

            opponent1Avatar.setFill(new ImagePattern(avatar1));
            opponent2Avatar.setFill(new ImagePattern(avatar2));
            opponent3Avatar.setFill(new ImagePattern(avatar3));
        } catch (Exception e) {
            System.err.println("Ошибка загрузки аватаров: " + e.getMessage());
        }
    }

    private Image createTextCardImage(int cardNumber) {
    int width = 80;
    int height = 120;
    WritableImage image = new WritableImage(width, height);
    PixelWriter writer = image.getPixelWriter();

    // Заполняем фон карты (чередуем цвета для наглядности)
    Color bgColor = Color.hsb((cardNumber * 10) % 360, 0.3, 0.9);
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            writer.setColor(x, y, bgColor);
        }
    }

    // Добавляем обводку
    for (int y = 0; y < height; y++) {
        writer.setColor(0, y, Color.BLACK);
        writer.setColor(width-1, y, Color.BLACK);
    }
    for (int x = 0; x < width; x++) {
        writer.setColor(x, 0, Color.BLACK);
        writer.setColor(x, height-1, Color.BLACK);
    }

    // Добавляем текст (номер карты) в центр
    Canvas canvas = new Canvas(width, height);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.BLACK);
    gc.setFont(Font.font(18));
    gc.fillText(String.valueOf(cardNumber), width/2 - 5, height/2 + 5);

    // Конвертируем Canvas в Image
    SnapshotParameters params = new SnapshotParameters();
    params.setFill(Color.TRANSPARENT);
    return canvas.snapshot(params, null);
}

    private void initTestData() {
        // Загрузка тестовых карт (в реальной игре это будет логика раздачи)
//        for (int i = 1; i <= 36; i++) {
//            try {
//                Image card = new Image(getClass().getResourceAsStream("/org/example/project101game/cards/card_" + i + ".png"));
//                playerCards.add(card);
//            } catch (Exception e) {
//                System.err.println("Не удалось загрузить карту " + i);
//            }
//        }

        playerCards.clear(); // Очищаем список карт

        // Создаем 36 тестовых карт с номерами вместо изображений
        for (int i = 1; i <= 36; i++) {
            // Создаем простую карту как цветной прямоугольник с текстом
            Image placeholderCard = createTextCardImage(i);
            playerCards.add(placeholderCard);
        }

        // Установка количества карт у противников
        opponent1Cards.setText("5");
        opponent2Cards.setText("7");
        opponent3Cards.setText("3");

        // Установка первой карты в сброс
        if (!playerCards.isEmpty()) {
            discardPileView.setImage(playerCards.get(0));
        }
    }

    private void showPlayerCardsPage() {
        playerHand.getChildren().clear();

        int startIndex = currentPage * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, playerCards.size());

        for (int i = startIndex; i < endIndex; i++) {
            ImageView cardView = new ImageView(playerCards.get(i));
            cardView.setFitWidth(80);
            cardView.setFitHeight(120);

            // Создаем финальную копию переменной для использования в лямбде
            final int cardIndex = i;
            cardView.setOnMouseClicked(event -> onCardClick(cardView, cardIndex));

            playerHand.getChildren().add(cardView);
        }
    }

    private void onCardClick(ImageView cardView, int cardIndex) {
        // Логика игры карты
        System.out.println("Играем карту #" + cardIndex);
        discardPileView.setImage(cardView.getImage());
        playerCards.remove(cardIndex);
        showPlayerCardsPage();
    }

    @FXML
    private void onChangeCardsClick() {
        // Переключение между страницами карт
        int maxPages = (int) Math.ceil((double) playerCards.size() / CARDS_PER_PAGE);
        currentPage = (currentPage + 1) % maxPages;
        showPlayerCardsPage();
    }

    @FXML
    private void onDeckClick() {
        // Взятие карты из колоды
        if (!playerCards.isEmpty()) {
            Image newCard = playerCards.get(0); // В реальной игре - случайная карта
            playerCards.add(newCard);
            showPlayerCardsPage();
        }
    }

}
