package myppt.render;

import java.awt.Color;
import java.util.Locale;

public final class Colors {
    private Colors() {}

    public static Color parse(String value, Color fallback) {
        if (value == null) return fallback;
        try {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "white" -> Color.WHITE;
                case "black" -> Color.BLACK;
                case "red" -> Color.RED;
                case "green" -> Color.GREEN;
                case "blue" -> Color.BLUE;
                case "gray", "grey" -> Color.GRAY;
                case "yellow" -> Color.YELLOW;
                default -> Color.decode(value);
            };
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
