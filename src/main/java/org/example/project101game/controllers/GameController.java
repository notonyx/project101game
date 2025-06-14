package org.example.project101game.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.project101game.GameClient;
import org.example.project101game.SceneSwitcher;
import org.example.project101game.models.Card;
import org.example.project101game.models.Rank;
import org.example.project101game.models.ServerCard;
import org.example.project101game.models.Suit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameController {

    @FXML
    private HBox playerHand;
    @FXML
    private ImageView deckView;
    @FXML
    private ImageView discardPileView;
    @FXML
    private VBox opponent1Box, opponent2Box, opponent3Box;
    @FXML
    private Circle opponent1Avatar, opponent2Avatar, opponent3Avatar;
    @FXML
    private Label opponent1Cards, opponent2Cards, opponent3Cards;

    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 9;
    private List<Card> deck = new ArrayList<>();
    private List<Card> playerHandCards = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();
    private GameClient client;
    private boolean isMyTurn;
    private Card currentCard;

    @FXML
    protected void onBackClick() {
        SceneSwitcher.switchTo("menu.fxml");
    }

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

    public void setIsMyTurn(boolean myTurn) {
        isMyTurn = myTurn;
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

    private boolean canPlay(Card card) {
        if (currentCard == null) {
            return true;
        }
        if (currentCard.getSuit().toString().equals(card.getSuit().toString()) || currentCard.getRank().toString().equals(card.getRank().toString())) {
            return true;
        }
        return false;
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
            writer.setColor(width - 1, y, Color.BLACK);
        }
        for (int x = 0; x < width; x++) {
            writer.setColor(x, 0, Color.BLACK);
            writer.setColor(x, height - 1, Color.BLACK);
        }

        // Добавляем текст (номер карты) в центр
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(18));
        gc.fillText(String.valueOf(cardNumber), width / 2 - 5, height / 2 + 5);

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

    private Suit showSuitSelectionDialog() {
        List<String> choices = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            choices.add(suit.toString());
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Выбор масти");
        dialog.setHeaderText("Вы сыграли королеву! Выберите масть:");
        dialog.setContentText("Масть:");

        Optional<String> result = dialog.showAndWait();
        return result.map(Suit::fromSymbol).orElse(null);
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
                if (!isMyTurn) {
                    System.out.println("Не ваш ход, нельзя играть карту.");
                    return;
                }
                if (!canPlay(card)) {
                    System.out.println("Вы не можете ходить данной картой");
                    return;
                }
                if (card.getRank().toString().equals("Q")) {
                    Suit selectedSuit = showSuitSelectionDialog();
                    System.out.println(selectedSuit.toString());
                    if (selectedSuit == null) return; // Диалог отменен

                    // Создаем "виртуальную" карту с выбранной мастью
                    Card virtualCard = new Card(selectedSuit, card.getRank(), card.getImage());
                    currentCard = virtualCard;
                    this.client.sendPlayCard(currentCard); // Отправляем оригинальную карту + выбранную масть
                } else {
                    currentCard = card;
                    this.client.sendPlayCard(currentCard); // Отправка без смены масти
                }
                Card playedCard = playerHandCards.remove(cardIndex);
//                this.client.setGameController(this); // посчитал лишним еще раз передавать в клиента контроллер, мы уже это делаем в waitingroomcontroller
                showPlayerCardsPage();
            });

            playerHand.getChildren().add(cardView);
        }
    }

    public void playedCard(String msg) {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                try {
                    String path = String.format("/org/example/project101game/cards/%s/%s.png",
                            suit.name().toLowerCase(),
                            rank.getFileName()); // Используем getFileName()
                    Image image = new Image(getClass().getResourceAsStream(path));
                    Card card = new Card(suit, rank, image);
                    if (msg.equals(card.getRank().toString() + " " + card.getSuit().toString())) {
                        discardPile.add(card);
                        discardPileView.setImage(card.getImage());
                    }
                } catch (Exception e) {
                    System.err.println("Не удалось загрузить карту: " + suit + "_" + rank);
                    // Создаем placeholder
                    Image placeholder = createCardPlaceholder(suit, rank);
                    deck.add(new Card(suit, rank, placeholder));
                }
            }
        }
    }

    @FXML
    private void onChangeCardsClick() {
        int maxPages = (int) Math.ceil((double) playerHandCards.size() / CARDS_PER_PAGE);
        currentPage = (currentPage + 1) % maxPages;
        showPlayerCardsPage();
    }


    /**
     * Вызывается сразу после загрузки game.fxml
     *
     * @param serverHand    список пришедших от сервера ServerCard (Suit + Rank)
     * @param myId          ваш уникальный идентификатор (IP или UUID)
     * @param currentTurnId — чей сейчас ход (тоже тот же идентификатор)
     */
    public void initGame(List<ServerCard> serverHand, String myId, String currentTurnId, GameClient client) {
        // 1. Сброс старой руки и сброса
        playerHandCards.clear();
        discardPile.clear();
        this.client = client;

        // 2. Преобразуем ServerCard → Card (с Image) и заполняем playerHandCards
        for (ServerCard sc : serverHand) {
            String suitName = sc.getSuit().name().toLowerCase();
            String rankFile = sc.getRank().getFileName();
            String path = String.format("/org/example/project101game/cards/%s/%s.png", suitName, rankFile);
            Image img;
            try {
                img = new Image(getClass().getResourceAsStream(path));
            } catch (Exception e) {
                img = createCardPlaceholder(sc.getSuit(), sc.getRank());
            }
            playerHandCards.add(new Card(sc.getSuit(), sc.getRank(), img));
        }
        deckView.setOnMouseClicked(event -> {
            if (!isMyTurn) {
                System.out.println("Не ваш ход, нельзя брать карту.");
                return;
            }
            // Отправляем серверу запрос на взятие карты
            client.sendDrawCard();

            System.out.println("Запрос на взятие карты отправлен серверу");
        });


        // 3. Показываем первую страницу руки
        currentPage = 0;
        showPlayerCardsPage();

        // 4. Устанавливаем вид рубашки колоды
        try {
            deckView.setImage(new Image(getClass().getResourceAsStream("/org/example/project101game/cards/card_back.png")));
            deckView.setVisible(true);
        } catch (Exception ignore) {
        }

        // 5. Обновляем статус хода
        setIsMyTurn(myId.equals(currentTurnId));
        disableDeckAndPlayerHand();
        // если не ваш ход, нельзя кликать по руке
        // Здесь можно также подсветить чей ход, например:
        if (isMyTurn) {
            System.out.println("Ваш ход!");
        } else {
            System.out.println("Ход другого игрока: " + currentTurnId);
        }
    }

    public void disableDeckAndPlayerHand() {
        deckView.setDisable(!isMyTurn);
        playerHand.setDisable(!isMyTurn);
    }



    public void onCardDrawn(ServerCard card) {
        disableDeckAndPlayerHand();
        String suitName = card.getSuit().name().toLowerCase();
        String rankFile = card.getRank().getFileName();
        String path = String.format("/org/example/project101game/cards/%s/%s.png", suitName, rankFile);
        Image img;
        try {
            img = new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            img = createCardPlaceholder(card.getSuit(), card.getRank());
        }

        Card newCard = new Card(card.getSuit(), card.getRank(), img);
        Platform.runLater(() -> {
            playerHandCards.add(newCard);
            showPlayerCardsPage();
        });
    }


}
