package main;


import com.formdev.flatlaf.FlatLightLaf;
import ui.AppFrame;

public class Main {
    public static void main(String[] args) {
        // 启动 FlatLaf
        FlatLightLaf.setup();

        javax.swing.SwingUtilities.invokeLater(() -> {
            new AppFrame().setVisible(true);
        });
    }
}
