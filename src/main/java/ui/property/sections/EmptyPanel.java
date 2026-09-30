package ui.property.sections;

import javax.swing.*;
import java.awt.*;

public class EmptyPanel extends JPanel {
    public EmptyPanel() {
        this("无选中元素");
    }

    public EmptyPanel(String text) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(new Color(245,245,245));

        add(new JLabel(text));
    }
}
