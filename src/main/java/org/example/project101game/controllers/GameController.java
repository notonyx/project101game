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
import java.util.Collections;
import java.util.List;

import org.example.project101game.models.Card;
import org.example.project101game.models.Rank;
import org.example.project101game.models.Suit;

public class GameController {

    @FXML private HBox playerHand;
    @FXML private ImageView deckView;
    @FXML private ImageView discardPileView;
    @FXML private VBox opponent1Box, opponent2Box, opponent3Box;
    @FXML private Circle opponent1Avatar, opponent2Avatar, opponent3Avatar;
    @FXML private Label opponent1Cards, opponent2Cards, opponent3Cards;

    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 9;
    private List<Card> deck = new ArrayList<>();
    private List<Card> playerHandCards = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();

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

    private Image createCardPlaceholder(Suit suit, Rank rank) {
        int width = 80;
        int height = 120;
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Фон зависит от масти
        Color bgColor = switch (suit) {
            case HEARTS, DIAMONDS -> Color.LIGHTPINK;
            case CLUBS, SPADES -> Color.LIGHTGRAY;
        };

        gc.setFill(bgColor);
        gc.fillRect(0, 0, width, height);

        // Текст карты
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText(rank.toString(), 10, 20);
        gc.fillText(suit.toString(), 10, 40);

        return canvas.snapshot(null, null);
    }

//    private Image createBackPlaceholder() {
//        int width = 80;
//        int height = 120;
//        Canvas canvas = new Canvas(width, height);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        gc.setFill(Color.DARKBLUE);
//        gc.fillRect(0, 0, width, height);
//        return canvas.snapshot(null, null);
//    }

    private void initializeDeck() {
        deck = new ArrayList<>(); // Инициализируем колоду

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                try {
                    String path = String.format("/org/example/project101game/cards/%s/%s.png",
                        suit.name().toLowerCase(),
                        rank.getFileName()); // Используем getFileName()
                    Image image = new Image(getClass().getResourceAsStream(path));
                    deck.add(new Card(suit, rank, image));
                } catch (Exception e) {
                    System.err.println("Не удалось загрузить карту: " + suit + "_" + rank);
                    // Создаем placeholder
                    Image placeholder = createCardPlaceholder(suit, rank);
                    deck.add(new Card(suit, rank, placeholder));
                }
            }
        }

        Collections.shuffle(deck); // Перемешиваем колоду

        // Загружаем рубашку
        try {
            Image backImage = new Image(getClass().getResourceAsStream("/org/example/project101game/cards/card_back.png"));
            deckView.setImage(backImage);
        } catch (Exception e) {
            System.err.println("Не удалось загрузить рубашку карты");
            //deckView.setImage(createBackPlaceholder());
        }
    }

    private void initTestData() {
        initializeDeck(); // Инициализируем колоду

        // Раздаем начальные карты игроку
        for (int i = 0; i < 6; i++) { // Раздаем 6 карт
            if (!deck.isEmpty()) {
                playerHandCards.add(deck.remove(0));
            }
        }

        // Установка количества карт у противников
        opponent1Cards.setText("5");
        opponent2Cards.setText("7");
        opponent3Cards.setText("3");

        // Установка первой карты в сброс
        if (!deck.isEmpty()) {
            Card firstCard = deck.remove(0);
            discardPile.add(firstCard);
            discardPileView.setImage(firstCard.getImage());
        }
    }

    private void showPlayerCardsPage() {
        playerHand.getChildren().clear();

        int startIndex = currentPage * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, playerHandCards.size());

        for (int i = startIndex; i < endIndex; i++) {
            Card card = playerHandCards.get(i);
            ImageView cardView = new ImageView(card.getImage());
            cardView.setFitWidth(120);
            cardView.setFitHeight(180);

            final int cardIndex = i;
            cardView.setOnMouseClicked(event -> {
                // Играем карту - перемещаем в сброс
                Card playedCard = playerHandCards.remove(cardIndex);
                discardPile.add(playedCard);
                discardPileView.setImage(playedCard.getImage());
                showPlayerCardsPage();
            });

            playerHand.getChildren().add(cardView);
        }
    }

    private void onCardClick(ImageView cardView, int cardIndex) {
        System.out.println("Играем карту #" + cardIndex);
        discardPileView.setImage(cardView.getImage());
        playerHandCards.remove(cardIndex);
        showPlayerCardsPage();
    }

    @FXML
    private void onChangeCardsClick() {
        int maxPages = (int) Math.ceil((double) playerHandCards.size() / CARDS_PER_PAGE);
        currentPage = (currentPage + 1) % maxPages;
        showPlayerCardsPage();
    }

    @FXML
    private void onDeckClick() {
        if (!deck.isEmpty()) {
            // Берем верхнюю карту
            Card drawnCard = deck.remove(0);
            playerHandCards.add(drawnCard);

            // Обновляем отображение
            showPlayerCardsPage();

            // Если колода пуста, скрываем ее
            if (deck.isEmpty()) {
                deckView.setVisible(false);
            }

            System.out.println("Взята карта: " + drawnCard);
        } else {
            System.out.println("Колода пуста!");
        }
    }

}
