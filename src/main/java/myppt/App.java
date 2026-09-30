package myppt;

import com.formdev.flatlaf.FlatLightLaf;
import myppt.ui.AppFrame;

import javax.swing.SwingUtilities;

public final class App {
    private App() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            new AppFrame().setVisible(true);
        });
    }
}
