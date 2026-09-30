package ui.property.sections;

import model.*;
import ui.AppFrame;
import ui.property.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

public class ShapePropertyPanel extends JPanel {

    public ShapePropertyPanel(PPTObject obj) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(245,245,245));

        add(new JLabel("图形属性"));

        /* ========= 边框颜色 ========= */
        JButton borderBtn = new JButton("边框颜色");
        borderBtn.addActionListener(e -> {
            Color c = ColorUtil.pickColor(null);
            if (c != null) {
                safeCall(obj, "setStrokeColor", ColorUtil.toHex(c));
                AppFrame.getInstance().getPropertyPanel().notifyCanvas(obj);

            }
        });
        add(borderBtn);

        /* ========= 填充颜色（线条无填充） ========= */
        if (!(obj instanceof PPTLine)) {
            // ========== 填充开关 ==========
            boolean init = false;
            try {
                Method getter = obj.getClass().getMethod("isFillEnabled");
                init = (boolean) getter.invoke(obj);
            } catch (Exception ignored) {}

            JCheckBox fillCheck = new JCheckBox("启用填充", init);
            fillCheck.addActionListener(e -> {
                try {
                    Method setter = obj.getClass().getMethod("setFillEnabled", boolean.class);
                    setter.invoke(obj, fillCheck.isSelected());
                } catch(Exception ignored) {}

                AppFrame.getInstance().getPropertyPanel().notifyCanvas(obj);
            });
            add(fillCheck);

            JButton fillBtn = new JButton("填充颜色");
            fillBtn.addActionListener(e -> {
                Color c = ColorUtil.pickColor(null);
                if (c != null) {
                    safeCall(obj, "setFillColor", ColorUtil.toHex(c));
                    AppFrame.getInstance().getPropertyPanel().notifyCanvas(obj);
                }
            });
            add(fillBtn);
        }
    }


    /** 安全调用 setter（方法不存在则跳过，不报错） */
    private void safeCall(PPTObject obj, String method, Object arg) {
        try {
            Method m = obj.getClass().getMethod(method, arg.getClass());
            m.invoke(obj, arg);
        } catch (Exception ignored) {}
    }

    /** 安全读取 int getter，方法不存在返回默认值 */
    private int safeGetInt(PPTObject obj, String method, int defaultValue) {
        try {
            Method m = obj.getClass().getMethod(method);
            return (int) m.invoke(obj);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
