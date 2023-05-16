package org.kalimbekov;

import org.telegram.telegrambots.meta.api.objects.User;

public class State {
    User user;

    private int variant;    // Variant of 0 actually means that the
                            // variant has not been selected yet

    private int length;     // Depending on the variant number
                            // we might need different number of arguments

    public State(User user) {
        this.user = user;
    }

    public String getNeededVariables() {
        return switch (this.variant) {
            case 1 -> "a, b, c, n, x";
            case 2 -> "a, ω, x, y";
            case 3 -> "a0, a1, a2, x";
            case 4 -> "a, x";
            case 5 -> "a, b, c, d, x";
            case 6 -> "x";
            case 7 -> "x";
            default -> throw new IllegalStateException("Unexpected value: " + this.variant);
        };
    }

    public User getUser() {
        return user;
    }

    public int getVariant() {
        return variant;
    }

    public void setVariant(int variant) {
        if (variant < 0 || variant > 7) {
            throw new IllegalArgumentException("""
                    Allowed values are in the following range: [0-7]
                    """);
        } else {
            this.variant = variant;
            this.length = switch (variant) {
                case 1 -> 5;
                case 2 -> 4;
                case 3 -> 4;
                case 4 -> 2;
                case 5 -> 5;
                case 6 -> 1;
                case 7 -> 1;
                default -> 0;
            };
        }
    }

    public int getLength() {
        return length;
    }
}
