package org.example.project101game.models;

import javafx.scene.image.Image;

public class Card {
    private final Suit suit;
    private final Rank rank;
    private final Image image;

    public Card(Suit suit, Rank rank, Image image) {
        this.suit = suit;
        this.rank = rank;
        this.image = image;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Image getImage() {
        return image;
    }

    public int getValue() {
        return rank.getValue();
    }

    @Override
    public String toString() {
        return rank + " " + suit;
    }
}