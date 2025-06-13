package org.example.project101game.models;

public enum Suit {
    HEARTS,
    DIAMONDS,
    CLUBS,
    SPADES;

    @Override
    public String toString() {
        return switch (this) {
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
            case SPADES -> "♠";
        };
    }
    // В enum Suit
    public static Suit fromSymbol(String symbol) {
        return switch (symbol) {
            case "♥" -> HEARTS;
            case "♦" -> DIAMONDS;
            case "♣" -> CLUBS;
            case "♠" -> SPADES;
            default -> throw new IllegalArgumentException("Unknown suit: " + symbol);
        };
    }

}
