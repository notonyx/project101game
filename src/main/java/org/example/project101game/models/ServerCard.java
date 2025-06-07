package org.example.project101game.models;

// GameServer -> Card.java
public class ServerCard {
    private final Suit suit;
    private final Rank rank;

    public ServerCard(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
