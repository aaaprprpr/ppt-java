package ui.menu;

import javax.swing.*;
import java.awt.*;

public class ToolBarPanel extends JPanel {
    private JButton deleteButton;
    private java.util.List<JButton> toolButtons = new java.util.ArrayList<>();

    public interface ToolSelectListener {
        void onToolSelected(String tool);
        void addImage(String path);  // 新增
    }

    private ToolSelectListener listener;

    public ToolBarPanel(ToolSelectListener listener) {
        this.listener = listener;

        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBackground(new Color(245, 245, 245));

        add(createButton("文本框", "text"));
        add(createButton("矩形",  "rect"));
        add(createButton("圆形",  "circle"));
        add(createButton("直线",  "line"));
        add(createButton("图片",  "image"));

        add(new JSeparator(SwingConstants.VERTICAL));

        deleteButton = createButton("删除", "delete");
        deleteButton.setEnabled(false);   // 初始禁用
        add(deleteButton);

    }

    private JButton createButton(String text, String toolValue) {
        JButton btn = new JButton(text);
        if (!toolValue.equals("delete")) toolButtons.add(btn);   // 删除按钮不加入全局禁用

        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            if (listener != null) {

                // 如果是图片按钮，直接选择文件
                if (toolValue.equals("image")) {
                    JFileChooser chooser = new JFileChooser();
                    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        String path = chooser.getSelectedFile().getAbsolutePath();
                        listener.addImage(path);   // 直接添加图片
                    }
                    return; // 不进入创建模式
                }

                // 其他工具正常进入创建模式
                listener.onToolSelected(toolValue);
            }

        });
        return btn;
    }
    public void setDeleteEnabled(boolean enabled) {
        deleteButton.setEnabled(enabled);
    }
    public void setEditingEnabled(boolean enabled) {
        for (JButton b : toolButtons) {
            b.setEnabled(enabled);
        }
    }

}
