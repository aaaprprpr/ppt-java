package ui.property;

import lombok.Data;
import lombok.EqualsAndHashCode;
import model.*;
import ui.AppFrame;
import ui.property.sections.*;

import javax.swing.*;
import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = false)
public class PropertyPanel extends JPanel {

    private JPanel content;   // 可变内容区域

    public PropertyPanel() {
        setPreferredSize(new Dimension(240, 0));
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        add(content, BorderLayout.NORTH);
        showEmpty();
    }

    /** 无选中对象时显示空面板 */
    public void showEmpty() {
        content.removeAll();
        content.add(new EmptyPanel());
        refresh();
    }

    /** 根据选中元素类型切换属性组件 */
    public void showProperties(PPTObject obj) {
        content.removeAll();

        if (obj == null) {
            showEmpty();
            return;
        }

        switch (obj.getType()) {
            case "rect":
            case "circle":
                content.add(new ShapePropertyPanel(obj));
                break;

            case "line":
                content.add(new ShapePropertyPanel(obj)); // 线条也共用，但不显示填充色
                break;

            case "text":
                content.add(new TextPropertyPanel((PPTText)obj));
                break;

            case "image":
                // 图片暂无属性
                content.add(new EmptyPanel("图片没有可设置的属性"));
                break;

            default:
                showEmpty();
        }
        refresh();
    }

    private void refresh() {
        revalidate();
        repaint();
    }
    public void notifyCanvas(PPTObject obj) {
        AppFrame.getInstance().getCanvasPanel().onPropertyChanged(obj);
    }

}
