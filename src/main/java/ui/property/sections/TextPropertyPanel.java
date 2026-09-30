package ui.property.sections;

import model.PPTText;
import ui.AppFrame;
import ui.property.ColorUtil;

import javax.swing.*;
import java.awt.*;

public class TextPropertyPanel extends JPanel {

    public TextPropertyPanel(PPTText obj) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(245,245,245));

        // 文字颜色
        JButton colorBtn = new JButton("文字颜色");
        colorBtn.addActionListener(e -> {
            Color c = ColorUtil.pickColor(null);
            if (c != null) {
                obj.setFontColor(ColorUtil.toHex(c));
                AppFrame.getInstance().getPropertyPanel().notifyCanvas(obj);

            }
        });

        // 字体大小
        JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(obj.getFontSize(), 8, 128, 1));
        fontSizeSpinner.addChangeListener(e -> {
            obj.setFontSize((int) fontSizeSpinner.getValue());
            AppFrame.getInstance().getPropertyPanel().notifyCanvas(obj);

        });

        add(new JLabel("文本属性"));
        add(colorBtn);

        // 字体选择
        // Win 下常见的三种：黑体、宋体、雅黑
        add(new JLabel("字体"));
        String[] fonts = {"SimHei", "SimSun", "Microsoft YaHei"};
        JComboBox<String> fontBox = new JComboBox<>(fonts);
        fontBox.setSelectedItem(obj.getFontFamily());
        fontBox.addActionListener(e -> {
            obj.setFontFamily((String) fontBox.getSelectedItem());
            AppFrame.getInstance().getPropertyPanel().notifyCanvas(obj);
        });
        add(fontBox);

        add(new JLabel("字号"));
        add(fontSizeSpinner);
    }
}
