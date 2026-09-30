package myppt.ui.property;

import myppt.model.*;
import myppt.render.Colors;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class PropertyPanel extends JPanel {
    private final Consumer<PPTObject> onChange;

    public PropertyPanel(Consumer<PPTObject> onChange) {
        this.onChange = onChange;
        setPreferredSize(new Dimension(230, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        showProperties(null);
    }

    public void showProperties(PPTObject object) {
        removeAll();
        if (object instanceof PPTText text) {
            add(new JLabel("文本属性"));
            colorButton("文字颜色", text.getFontColor(), value -> {
                text.setFontColor(value);
                onChange.accept(text);
            });
            add(new JLabel("字体"));
            JComboBox<String> fonts = new JComboBox<>(new String[]{"SansSerif", "SimHei", "SimSun", "Microsoft YaHei"});
            fonts.setEditable(true);
            fonts.setSelectedItem(text.getFontFamily());
            fonts.addActionListener(e -> {
                Object family = fonts.getSelectedItem();
                if (family != null && !family.toString().isBlank()) {
                    text.setFontFamily(family.toString());
                    onChange.accept(text);
                }
            });
            addField(fonts);
            add(new JLabel("字号"));
            int size = Math.max(1, text.getFontSize());
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(size, 1, Math.max(512, size), 1));
            spinner.addChangeListener(e -> {
                text.setFontSize((int) spinner.getValue());
                onChange.accept(text);
            });
            addField(spinner);
        } else if (object instanceof PPTShape shape) {
            add(new JLabel("图形属性"));
            colorButton("边框颜色", shape.getStrokeColor(), value -> {
                shape.setStrokeColor(value);
                onChange.accept(shape);
            });
            JCheckBox filled = new JCheckBox("启用填充", shape.isFillEnabled());
            filled.addActionListener(e -> {
                shape.setFillEnabled(filled.isSelected());
                onChange.accept(shape);
            });
            add(filled);
            colorButton("填充颜色", shape.getFillColor(), value -> {
                shape.setFillColor(value);
                onChange.accept(shape);
            });
        } else if (object instanceof PPTLine line) {
            add(new JLabel("直线属性"));
            colorButton("线条颜色", line.getStrokeColor(), value -> {
                line.setStrokeColor(value);
                onChange.accept(line);
            });
        } else {
            add(new JLabel(object instanceof PPTImage ? "拖动图片边框可调整大小" : "无选中元素"));
        }
        revalidate();
        repaint();
    }

    private void addField(JComponent field) {
        field.setMaximumSize(new Dimension(200, field.getPreferredSize().height));
        field.setAlignmentX(LEFT_ALIGNMENT);
        add(field);
    }

    private void colorButton(String title, String initial, Consumer<String> update) {
        JButton button = new JButton(title);
        button.setForeground(Colors.parse(initial, Color.BLACK));
        button.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, title, button.getForeground());
            if (color != null) {
                button.setForeground(color);
                update.accept(Colors.toHex(color));
            }
        });
        add(button);
    }
}
