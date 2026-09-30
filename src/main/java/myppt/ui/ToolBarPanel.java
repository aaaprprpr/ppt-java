package myppt.ui;

import myppt.ui.canvas.CanvasPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class ToolBarPanel extends JPanel {
    private final List<JButton> editingButtons = new ArrayList<>();
    private final JButton delete;

    ToolBarPanel(CanvasPanel canvas, Runnable addImage) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addTool("文本框", () -> canvas.selectTool(CanvasPanel.Tool.TEXT));
        addTool("矩形", () -> canvas.selectTool(CanvasPanel.Tool.RECT));
        addTool("圆形", () -> canvas.selectTool(CanvasPanel.Tool.CIRCLE));
        addTool("直线", () -> canvas.selectTool(CanvasPanel.Tool.LINE));
        addTool("图片", addImage);
        delete = new JButton("删除");
        delete.addActionListener(e -> canvas.deleteSelected());
        delete.setEnabled(false);
        add(delete);
        setEditingEnabled(false);
    }

    private void addTool(String title, Runnable action) {
        JButton button = new JButton(title);
        button.addActionListener(e -> action.run());
        editingButtons.add(button);
        add(button);
    }

    void setEditingEnabled(boolean enabled) {
        editingButtons.forEach(button -> button.setEnabled(enabled));
        if (!enabled) delete.setEnabled(false);
    }

    void setDeleteEnabled(boolean enabled) { delete.setEnabled(enabled); }
}
