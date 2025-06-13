package org.example.project101game.models;

public enum Rank {
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("jack", 2),    // Валет = 2 очка
    QUEEN("queen", 3),  // Дама = 3
    KING("king", 4),    // Король = 4
    ACE("ace", 11);     // Туз = 11

    private final String fileName;
    private final int value;

    Rank(String fileName, int value) {
        this.fileName = fileName;
        this.value = value;
    }

    public String getFileName() {
        return fileName;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return switch (this) {
            case SIX, SEVEN, EIGHT, NINE, TEN -> fileName;
            case JACK -> "J";
            case QUEEN -> "Q";
            case KING -> "K";
            case ACE -> "A";
        };
    }

    // В enum Rank
    public static Rank fromSymbol(String symbol) {
        return switch (symbol) {
            case "6" -> SIX;
            case "7" -> SEVEN;
            case "8" -> EIGHT;
            case "9" -> NINE;
            case "10" -> TEN;
            case "J" -> JACK;
            case "Q" -> QUEEN;
            case "K" -> KING;
            case "A" -> ACE;
            default -> throw new IllegalArgumentException("Unknown rank: " + symbol);
        };
    }

}