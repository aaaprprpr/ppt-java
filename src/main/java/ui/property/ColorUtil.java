package ui.property;

import javax.swing.*;
import java.awt.*;

public class ColorUtil {

    public static Color pickColor(Component parent) {
        return JColorChooser.showDialog(parent, "选择颜色", Color.BLACK);
    }

    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
